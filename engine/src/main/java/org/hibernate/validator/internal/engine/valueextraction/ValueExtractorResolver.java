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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.TypeVariableBindings;
import org.hibernate.validator.internal.util.TypeVariables;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Contains resolving algorithms for {@link ValueExtractor}s, and caches for these
 * extractors based on container types.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
class ValueExtractorResolver {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final Object NON_CONTAINER_VALUE = new Object();

	private final ConcurrentHashMap<ValueExtractorCacheKey, Set<ValueExtractorDescriptor>> possibleValueExtractorsByRuntimeTypeAndTypeParameter = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Class<?>, Set<ValueExtractorDescriptor>> possibleValueExtractorsByRuntimeType = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Class<?>, Object> nonContainerTypes = new ConcurrentHashMap<>();

	private final List<ValueExtractorDescriptor> valueExtractors;

	ValueExtractorResolver(List<ValueExtractorDescriptor> valueExtractors) {
		this.valueExtractors = valueExtractors;
	}

	/**
	 * Returns the maximally specific type compliant value extractors or an empty set if none was found.
	 */
	public Set<ValueExtractorDescriptor> getMaximallySpecificValueExtractors(Class<?> valueType) {
		return getRuntimeComplaintValueExtractors( valueType );
	}

	/**
	 * Returns the maximally specific type-compliant and container-element-compliant value extractor or
	 * {@code null} if none was found.
	 * <p>
	 * Throws an exception if more than 2 maximally specific container-element-compliant value extractors are found.
	 */
	public ValueExtractorDescriptor getMaximallySpecificAndContainerElementCompliantValueExtractor(Class<?> declaredType, TypeVariable<?> typeParameter) {
		return getUniqueValueExtractorOrThrowException(
				declaredType,
				getRuntimeAndContainerElementComplaintValueExtractorsFromPossibleCandidates( declaredType, typeParameter, declaredType, valueExtractors )
		);
	}

	/**
	 * Returns the maximally specific runtime-type-compliant and container-element-compliant value extractor or
	 * {@code null} if none was found.
	 * <p>
	 * Throws an exception if more than 2 maximally specific container-element-compliant value extractors are found.
	 */
	public ValueExtractorDescriptor getMaximallySpecificAndRuntimeContainerElementCompliantValueExtractor(Type declaredType, TypeVariable<?> typeParameter,
			Class<?> runtimeType, Collection<ValueExtractorDescriptor> valueExtractorCandidates) {
		return getUniqueValueExtractorOrThrowException(
				runtimeType,
				getRuntimeAndContainerElementComplaintValueExtractorsFromPossibleCandidates(
						declaredType, typeParameter, runtimeType, valueExtractorCandidates
				)
		);
	}

	/**
	 * Returns the maximally specific runtime-type-compliant value extractor or {@code null} if none was found.
	 * <p>
	 * Throws an exception if more than 2 maximally specific container-element-compliant value extractors are found.
	 */
	public ValueExtractorDescriptor getMaximallySpecificValueExtractorForAllContainerElements(Class<?> runtimeType) {
		// if it's a Map assignable type it gets a special treatment to support legacy containers
		if ( TypeHelper.isAssignable( Map.class, runtimeType ) ) {
			return MapValueExtractor.DESCRIPTOR;
		}
		return getUniqueValueExtractorOrThrowException( runtimeType, getRuntimeComplaintValueExtractors( runtimeType ) );
	}

	public Set<ValueExtractorDescriptor> getValueExtractorCandidatesForCascadedValidation(Type declaredType, TypeVariable<?> typeParameter) {
		Set<ValueExtractorDescriptor> valueExtractorDescriptors = new HashSet<>();

		valueExtractorDescriptors.addAll( getRuntimeAndContainerElementComplaintValueExtractorsFromPossibleCandidates( declaredType, typeParameter,
				TypeHelper.getErasedReferenceType( declaredType ), valueExtractors
		) );
		valueExtractorDescriptors.addAll( getPotentiallyRuntimeTypeCompliantAndContainerElementCompliantValueExtractors( declaredType, typeParameter ) );

		return CollectionHelper.toImmutableSet( valueExtractorDescriptors );
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

		Set<ValueExtractorDescriptor> typeCompatibleExtractors = valueExtractors
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
		if ( maximallySpecificContainerElementCompliantValueExtractors.isEmpty() ) {
			return null;
		}
		else if ( maximallySpecificContainerElementCompliantValueExtractors.size() == 1 ) {
			return maximallySpecificContainerElementCompliantValueExtractors.iterator().next();
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
	 * @return a set of runtime complaint value extractors based on a runtime type. If there are no available value extractors
	 * an empty set will be returned which means a type is not a container.
	 */
	private Set<ValueExtractorDescriptor> getRuntimeComplaintValueExtractors(Class<?> runtimeType) {
		if ( nonContainerTypes.contains( runtimeType ) ) {
			return Collections.emptySet();
		}
		Set<ValueExtractorDescriptor> valueExtractorDescriptors = possibleValueExtractorsByRuntimeType.get( runtimeType );
		if ( valueExtractorDescriptors == null ) {
			//otherwise we just look for maximally specific extractors for the runtime type
			Set<ValueExtractorDescriptor> possibleValueExtractors = valueExtractors
					.stream()
					.filter( e -> TypeHelper.isAssignable( e.getContainerType(), runtimeType ) )
					.collect( Collectors.toSet() );

			valueExtractorDescriptors = getMaximallySpecificValueExtractors( possibleValueExtractors );
		}
		if ( valueExtractorDescriptors.isEmpty() ) {
			nonContainerTypes.put( runtimeType, NON_CONTAINER_VALUE );
		}
		else {
			Set<ValueExtractorDescriptor> extractorDescriptorsToCache = CollectionHelper.toImmutableSet( valueExtractorDescriptors );
			possibleValueExtractorsByRuntimeType.put( runtimeType, extractorDescriptorsToCache );
			return extractorDescriptorsToCache;
		}

		return valueExtractorDescriptors;
	}

	private Set<ValueExtractorDescriptor> getRuntimeAndContainerElementComplaintValueExtractorsFromPossibleCandidates(Type declaredType,
			TypeVariable<?> typeParameter, Class<?> runtimeType, Collection<ValueExtractorDescriptor> valueExtractorCandidates) {
		if ( nonContainerTypes.contains( runtimeType ) ) {
			return Collections.emptySet();
		}
		ValueExtractorCacheKey cacheKey = new ValueExtractorCacheKey( runtimeType, typeParameter );

		Set<ValueExtractorDescriptor> valueExtractorDescriptors = possibleValueExtractorsByRuntimeTypeAndTypeParameter.get( cacheKey );
		if ( valueExtractorDescriptors == null ) {
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
				nonContainerTypes.put( runtimeType, NON_CONTAINER_VALUE );
			}
			else {
				Set<ValueExtractorDescriptor> extractorDescriptorsToCache = CollectionHelper.toImmutableSet( valueExtractorDescriptors );
				possibleValueExtractorsByRuntimeTypeAndTypeParameter.put( cacheKey, extractorDescriptorsToCache );
				return extractorDescriptorsToCache;
			}
		}

		return valueExtractorDescriptors;
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

		private final Class<?> type;
		private final TypeVariable<?> typeParameter;
		private final int hashCode;

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
			if ( o == null || this.getClass() != o.getClass() ) {
				return false;
			}
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
