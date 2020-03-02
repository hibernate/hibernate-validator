/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.valueextraction;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.aggregated.ContainerCascadingMetaData;
import org.hibernate.validator.internal.metadata.aggregated.PotentiallyContainerCascadingMetaData;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeVariableBindings;
import org.hibernate.validator.internal.util.TypeVariables;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Contains resolving algorithms for {@link ValueExtractor}s, and caches for these
 * extractors based on container types.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class ValueExtractorResolver {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	@Immutable
	private final Set<ValueExtractorDescriptor> registeredValueExtractors;

	private final ConcurrentHashMap<ValueExtractorCacheKey, Set<ValueExtractorDescriptor>> possibleValueExtractorsByRuntimeTypeAndTypeParameter = new ConcurrentHashMap<>();

	private final ConcurrentHashMap<Class<?>, Set<ValueExtractorDescriptor>> possibleValueExtractorsByRuntimeType = new ConcurrentHashMap<>();

	private final Set<Class<?>> nonContainerTypes = Collections.newSetFromMap( new ConcurrentHashMap<>() );

	ValueExtractorResolver(Set<ValueExtractorDescriptor> valueExtractors) {
		this.registeredValueExtractors = CollectionHelper.toImmutableSet( valueExtractors );
	}

	/**
	 * Used to find all the maximally specific value extractors based on a declared type in the case of value unwrapping.
	 * <p>
	 * There might be several of them as there might be several type parameters.
	 * <p>
	 * Used for container element constraints.
	 */
	public Set<ValueExtractorDescriptor> getMaximallySpecificValueExtractors(Class<?> declaredType) {
		return getRuntimeCompliantValueExtractors( declaredType, registeredValueExtractors );
	}

	/**
	 * Used to find the maximally specific and container element compliant value extractor based on the declared type
	 * and the type parameter.
	 * <p>
	 * Used for container element constraints.
	 *
	 * @throws ConstraintDeclarationException if more than 2 maximally specific container-element-compliant value extractors are found
	 */
	public ValueExtractorDescriptor getMaximallySpecificAndContainerElementCompliantValueExtractor(Class<?> declaredType, TypeVariable<?> typeParameter) {
		return getUniqueValueExtractorOrThrowException(
				declaredType,
				getRuntimeAndContainerElementCompliantValueExtractorsFromPossibleCandidates( declaredType, typeParameter, declaredType, registeredValueExtractors )
		);
	}

	/**
	 * Used to find the maximally specific and container element compliant value extractor based on the runtime type.
	 * <p>
	 * The maximally specific one is chosen among the candidates passed to this method.
	 * <p>
	 * Used for cascading validation.
	 *
	 * @see ValueExtractorResolver#getMaximallySpecificAndRuntimeContainerElementCompliantValueExtractor(Type,
	 * TypeVariable, Class, Collection)
	 * @throws ConstraintDeclarationException if more than 2 maximally specific container-element-compliant value extractors are found
	 */
	public ValueExtractorDescriptor getMaximallySpecificAndRuntimeContainerElementCompliantValueExtractor(Type declaredType, TypeVariable<?> typeParameter,
			Class<?> runtimeType, Collection<ValueExtractorDescriptor> valueExtractorCandidates) {
		Contracts.assertNotEmpty( valueExtractorCandidates, "Value extractor candidates cannot be empty" );
		if ( valueExtractorCandidates.size() == 1 ) {
			return valueExtractorCandidates.iterator().next();
		}
		else {
			return getUniqueValueExtractorOrThrowException(
					runtimeType,
					getRuntimeAndContainerElementCompliantValueExtractorsFromPossibleCandidates(
							declaredType, typeParameter, runtimeType, valueExtractorCandidates
					)
			);
		}
	}

	/**
	 * Used to determine if the passed runtime type is a container and if so return a corresponding maximally specific
	 * value extractor.
	 * <p>
	 * Obviously, it only works if there's only one value extractor corresponding to the runtime type as we don't
	 * precise any type parameter.
	 * <p>
	 * There is a special case: when the passed type is assignable to a {@link Map}, the {@link MapValueExtractor} will
	 * be returned. This is required by the Bean Validation specification.
	 * <p>
	 * Used for cascading validation when the {@code @Valid} annotation is placed on the whole container.
	 *
	 * @throws ConstraintDeclarationException if more than 2 maximally specific container-element-compliant value extractors are found
	 */
	public ValueExtractorDescriptor getMaximallySpecificValueExtractorForAllContainerElements(Class<?> runtimeType, Set<ValueExtractorDescriptor> potentialValueExtractorDescriptors) {
		// if it's a Map assignable type, it gets a special treatment to conform to the Bean Validation specification
		if ( TypeHelper.isAssignable( Map.class, runtimeType ) ) {
			return MapValueExtractor.DESCRIPTOR;
		}

		return getUniqueValueExtractorOrThrowException( runtimeType, getRuntimeCompliantValueExtractors( runtimeType, potentialValueExtractorDescriptors ) );
	}

	/**
	 * Used to determine the value extractor candidates valid for a declared type and type variable.
	 * <p>
	 * The effective value extractor will be narrowed from these candidates using the runtime type.
	 * <p>
	 * Used to optimize the choice of the value extractor in the case of cascading validation.
	 */
	public Set<ValueExtractorDescriptor> getValueExtractorCandidatesForCascadedValidation(Type declaredType, TypeVariable<?> typeParameter) {
		Set<ValueExtractorDescriptor> valueExtractorDescriptors = new HashSet<>();

		valueExtractorDescriptors.addAll( getRuntimeAndContainerElementCompliantValueExtractorsFromPossibleCandidates( declaredType, typeParameter,
				TypeHelper.getErasedReferenceType( declaredType ), registeredValueExtractors
		) );
		valueExtractorDescriptors.addAll( getPotentiallyRuntimeTypeCompliantAndContainerElementCompliantValueExtractors( declaredType, typeParameter ) );

		return CollectionHelper.toImmutableSet( valueExtractorDescriptors );
	}

	/**
	 * Used to determine the possible value extractors that can be applied to a declared type.
	 * <p>
	 * Used when building cascading metadata in {@link CascadingMetaDataBuilder} to decide if it should be promoted to
	 * {@link ContainerCascadingMetaData} with cascaded constrained type arguments.
	 * <p>
	 * An example could be when we need to upgrade BV 1.1 style {@code @Valid private List<SomeBean> list;}
	 * to {@code private List<@Valid SomeBean> list;}
	 * <p>
	 * Searches only for maximally specific value extractors based on a type.
	 * <p>
	 * Types that are assignable to {@link Map} are handled as a special case - key value extractor is ignored for them.
	 */
	public Set<ValueExtractorDescriptor> getValueExtractorCandidatesForContainerDetectionOfGlobalCascadedValidation(Type enclosingType) {
		// if it's a Map assignable type, it gets a special treatment to conform to the Bean Validation specification
		boolean mapAssignable = TypeHelper.isAssignable( Map.class, enclosingType );

		Class<?> enclosingClass = ReflectionHelper.getClassFromType( enclosingType );
		return getRuntimeCompliantValueExtractors( enclosingClass, registeredValueExtractors )
				.stream()
				.filter( ved -> !mapAssignable || !ved.equals( MapKeyExtractor.DESCRIPTOR ) )
				.collect( Collectors.collectingAndThen( Collectors.toSet(), CollectionHelper::toImmutableSet ) );
	}

	/**
	 * Used to determine the value extractors which potentially could be applied to the runtime type of a given declared type.
	 * <p>
	 * An example could be when there's a declaration like {@code private PotentiallyContainerAtRuntime<@Valid Bean>;} and there's
	 * no value extractor present for {@code PotentiallyContainerAtRuntime} but there's one available for
	 * {@code Container extends PotentiallyContainerAtRuntime}.
	 * <p>
	 * Returned set of extractors is used to determine if at runtime a value extractor can be applied to a runtime type,
	 * and if {@link PotentiallyContainerCascadingMetaData} should be promoted to {@link ContainerCascadingMetaData}.
	 *
	 * @return a set of {@link ValueExtractorDescriptor}s that possibly might be applied to a {@code declaredType}
	 * at a runtime.
	 */
	public Set<ValueExtractorDescriptor> getPotentialValueExtractorCandidatesForCascadedValidation(Type declaredType) {
		return registeredValueExtractors
				.stream()
				.filter( e -> TypeHelper.isAssignable( declaredType, e.getContainerType() ) )
				.collect( Collectors.collectingAndThen( Collectors.toSet(), CollectionHelper::toImmutableSet ) );
	}

	public void clear() {
		nonContainerTypes.clear();
		possibleValueExtractorsByRuntimeType.clear();
		possibleValueExtractorsByRuntimeTypeAndTypeParameter.clear();
	}

	/**
	 * Returns the set of potentially type-compliant and container-element-compliant value extractors or an empty set if none was found.
	 * <p>
	 * A value extractor is potentially runtime type compliant if it might be compliant for any runtime type that matches the declared type.
	 */
	private Set<ValueExtractorDescriptor> getPotentiallyRuntimeTypeCompliantAndContainerElementCompliantValueExtractors(Type declaredType,
			TypeVariable<?> typeParameter) {
		boolean isInternal = TypeVariables.isInternal( typeParameter );
		Type erasedDeclaredType = TypeHelper.getErasedReferenceType( declaredType );

		Set<ValueExtractorDescriptor> typeCompatibleExtractors = registeredValueExtractors
				.stream()
				.filter( e -> TypeHelper.isAssignable( erasedDeclaredType, e.getContainerType() ) )
				.collect( Collectors.toSet() );

		Set<ValueExtractorDescriptor> containerElementCompliantExtractors = new HashSet<>();

		for ( ValueExtractorDescriptor extractorDescriptor : typeCompatibleExtractors ) {
			TypeVariable<?> typeParameterBoundToExtractorType;

			if ( !isInternal ) {
				Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings =
						TypeVariableBindings.getTypeVariableBindings( extractorDescriptor.getContainerType() );

				Map<TypeVariable<?>, TypeVariable<?>> bindingsForExtractorType = allBindings.get( erasedDeclaredType );
				typeParameterBoundToExtractorType = bind( extractorDescriptor.getExtractedTypeParameter(), bindingsForExtractorType );
			}
			else {
				typeParameterBoundToExtractorType = typeParameter;
			}

			if ( Objects.equals( typeParameter, typeParameterBoundToExtractorType ) ) {
				containerElementCompliantExtractors.add( extractorDescriptor );
			}
		}

		return containerElementCompliantExtractors;
	}

	private ValueExtractorDescriptor getUniqueValueExtractorOrThrowException(Class<?> runtimeType,
			Set<ValueExtractorDescriptor> maximallySpecificContainerElementCompliantValueExtractors) {
		if ( maximallySpecificContainerElementCompliantValueExtractors.size() == 1 ) {
			return maximallySpecificContainerElementCompliantValueExtractors.iterator().next();
		}
		else if ( maximallySpecificContainerElementCompliantValueExtractors.isEmpty() ) {
			return null;
		}
		else {
			throw LOG.getUnableToGetMostSpecificValueExtractorDueToSeveralMaximallySpecificValueExtractorsDeclaredException( runtimeType,
					ValueExtractorHelper.toValueExtractorClasses( maximallySpecificContainerElementCompliantValueExtractors )
			);
		}
	}

	private Set<ValueExtractorDescriptor> getMaximallySpecificValueExtractors(Set<ValueExtractorDescriptor> possibleValueExtractors) {
		Set<ValueExtractorDescriptor> valueExtractorDescriptors = CollectionHelper.newHashSet( possibleValueExtractors.size() );

		for ( ValueExtractorDescriptor descriptor : possibleValueExtractors ) {
			if ( valueExtractorDescriptors.isEmpty() ) {
				valueExtractorDescriptors.add( descriptor );
				continue;
			}
			Iterator<ValueExtractorDescriptor> candidatesIterator = valueExtractorDescriptors.iterator();
			boolean isNewRoot = true;
			while ( candidatesIterator.hasNext() ) {
				ValueExtractorDescriptor candidate = candidatesIterator.next();

				// we consider the strictly more specific value extractor so 2 value extractors for the same container
				// type should throw an error in the end if no other more specific value extractor is found.
				if ( candidate.getContainerType().equals( descriptor.getContainerType() ) ) {
					continue;
				}

				if ( TypeHelper.isAssignable( candidate.getContainerType(), descriptor.getContainerType() ) ) {
					candidatesIterator.remove();
				}
				else if ( TypeHelper.isAssignable( descriptor.getContainerType(), candidate.getContainerType() ) ) {
					isNewRoot = false;
				}
			}
			if ( isNewRoot ) {
				valueExtractorDescriptors.add( descriptor );
			}
		}
		return valueExtractorDescriptors;
	}

	/**
	 * @return a set of runtime compliant value extractors based on a runtime type. If there are no available value extractors
	 * an empty set will be returned which means the type is not a container.
	 */
	private Set<ValueExtractorDescriptor> getRuntimeCompliantValueExtractors(Class<?> runtimeType, Set<ValueExtractorDescriptor> potentialValueExtractorDescriptors) {
		if ( nonContainerTypes.contains( runtimeType ) ) {
			return Collections.emptySet();
		}

		Set<ValueExtractorDescriptor> valueExtractorDescriptors = possibleValueExtractorsByRuntimeType.get( runtimeType );

		if ( valueExtractorDescriptors != null ) {
			return valueExtractorDescriptors;
		}

		Set<ValueExtractorDescriptor> possibleValueExtractors = potentialValueExtractorDescriptors
				.stream()
				.filter( e -> TypeHelper.isAssignable( e.getContainerType(), runtimeType ) )
				.collect( Collectors.toSet() );

		valueExtractorDescriptors = getMaximallySpecificValueExtractors( possibleValueExtractors );

		if ( valueExtractorDescriptors.isEmpty() ) {
			nonContainerTypes.add( runtimeType );
			return Collections.emptySet();
		}

		Set<ValueExtractorDescriptor> valueExtractorDescriptorsToCache = CollectionHelper.toImmutableSet( valueExtractorDescriptors );
		Set<ValueExtractorDescriptor> cachedValueExtractorDescriptors = possibleValueExtractorsByRuntimeType.putIfAbsent( runtimeType,
				valueExtractorDescriptorsToCache );
		return cachedValueExtractorDescriptors != null ? cachedValueExtractorDescriptors : valueExtractorDescriptorsToCache;
	}

	private Set<ValueExtractorDescriptor> getRuntimeAndContainerElementCompliantValueExtractorsFromPossibleCandidates(Type declaredType,
			TypeVariable<?> typeParameter, Class<?> runtimeType, Collection<ValueExtractorDescriptor> valueExtractorCandidates) {
		if ( nonContainerTypes.contains( runtimeType ) ) {
			return Collections.emptySet();
		}

		ValueExtractorCacheKey cacheKey = new ValueExtractorCacheKey( runtimeType, typeParameter );

		Set<ValueExtractorDescriptor> valueExtractorDescriptors = possibleValueExtractorsByRuntimeTypeAndTypeParameter.get( cacheKey );

		if ( valueExtractorDescriptors != null ) {
			return valueExtractorDescriptors;
		}

		boolean isInternal = TypeVariables.isInternal( typeParameter );
		Class<?> erasedDeclaredType = TypeHelper.getErasedReferenceType( declaredType );

		Set<ValueExtractorDescriptor> possibleValueExtractors = valueExtractorCandidates
				.stream()
				.filter( e -> TypeHelper.isAssignable( e.getContainerType(), runtimeType ) )
				.filter( extractorDescriptor ->
						checkValueExtractorTypeCompatibility(
								typeParameter, isInternal, erasedDeclaredType, extractorDescriptor
						)
				).collect( Collectors.toSet() );

		valueExtractorDescriptors = getMaximallySpecificValueExtractors( possibleValueExtractors );

		if ( valueExtractorDescriptors.isEmpty() ) {
			nonContainerTypes.add( runtimeType );
			return Collections.emptySet();
		}

		Set<ValueExtractorDescriptor> valueExtractorDescriptorsToCache = CollectionHelper.toImmutableSet( valueExtractorDescriptors );
		Set<ValueExtractorDescriptor> cachedValueExtractorDescriptors = possibleValueExtractorsByRuntimeTypeAndTypeParameter.putIfAbsent( cacheKey,
				valueExtractorDescriptorsToCache );
		return cachedValueExtractorDescriptors != null ? cachedValueExtractorDescriptors : valueExtractorDescriptorsToCache;
	}

	private boolean checkValueExtractorTypeCompatibility(TypeVariable<?> typeParameter, boolean isInternal, Class<?> erasedDeclaredType,
			ValueExtractorDescriptor extractorDescriptor) {
		return TypeHelper.isAssignable( extractorDescriptor.getContainerType(), erasedDeclaredType )
				? validateValueExtractorCompatibility( isInternal, erasedDeclaredType, extractorDescriptor.getContainerType(), typeParameter,
				extractorDescriptor.getExtractedTypeParameter()
		)
				: validateValueExtractorCompatibility( isInternal, extractorDescriptor.getContainerType(), erasedDeclaredType,
				extractorDescriptor.getExtractedTypeParameter(), typeParameter
		);
	}

	private boolean validateValueExtractorCompatibility(boolean isInternal,
			Class<?> typeForBinding,
			Class<?> typeToBind,
			TypeVariable<?> typeParameterForBinding,
			TypeVariable<?> typeParameterToCompare) {
		TypeVariable<?> typeParameterBoundToExtractorType;

		if ( !isInternal ) {
			Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> allBindings =
					TypeVariableBindings.getTypeVariableBindings( typeForBinding );

			Map<TypeVariable<?>, TypeVariable<?>> bindingsForExtractorType = allBindings.get( typeToBind );
			typeParameterBoundToExtractorType = bind( typeParameterForBinding, bindingsForExtractorType );
		}
		else {
			typeParameterBoundToExtractorType = typeParameterForBinding;
		}

		return Objects.equals( typeParameterToCompare, typeParameterBoundToExtractorType );
	}

	private TypeVariable<?> bind(TypeVariable<?> typeParameter, Map<TypeVariable<?>, TypeVariable<?>> bindings) {
		return bindings != null ? bindings.get( typeParameter ) : null;
	}

	private static class ValueExtractorCacheKey {

		// These properties are not final on purpose, it's faster when they are not

		private Class<?> type;
		private TypeVariable<?> typeParameter;
		private int hashCode;

		ValueExtractorCacheKey(Class<?> type, TypeVariable<?> typeParameter) {
			this.type = type;
			this.typeParameter = typeParameter;
			this.hashCode = buildHashCode();
		}

		@Override
		public boolean equals(Object o) {
			if ( this == o ) {
				return true;
			}
			if ( o == null ) {
				return false;
			}
			// We don't check the class as an optimization, the keys of the map are ValueExtractorCacheKey anyway
			ValueExtractorCacheKey that = (ValueExtractorCacheKey) o;
			return Objects.equals( this.type, that.type ) &&
					Objects.equals( this.typeParameter, that.typeParameter );
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		private int buildHashCode() {
			int result = this.type.hashCode();
			result = 31 * result + ( this.typeParameter != null ? this.typeParameter.hashCode() : 0 );
			return result;
		}
	}
}
