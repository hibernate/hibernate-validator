/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.GroupSequence;

import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorHelper;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeVariableBindings;
import org.hibernate.validator.internal.util.TypeVariables;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * A temporary data structure used to build {@link CascadingMetaData}. It is not a builder per se but it's as much as it
 * gets.
 *
 * @author Guillaume Smet
 */
public class CascadingMetaDataBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final CascadingMetaDataBuilder NON_CASCADING =
			new CascadingMetaDataBuilder( null, null, null, null, false, Collections.emptyMap(), Collections.emptyMap() );

	/**
	 * The enclosing type that defines this type parameter.
	 */
	private final Type enclosingType;

	/**
	 * The type parameter.
	 */
	private final TypeVariable<?> typeParameter;

	/**
	 * The declared container class: it is the one used in the node of the property path.
	 */
	private final Class<?> declaredContainerClass;

	/**
	 * The declared type parameter: it is the one used in the node of the property path.
	 */
	private final TypeVariable<?> declaredTypeParameter;

	/**
	 * Possibly the cascading type parameters corresponding to this type parameter if it is a parameterized type.
	 */
	@Immutable
	private final Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData;

	/**
	 * If this type parameter is marked for cascading.
	 */
	private final boolean cascading;

	/**
	 * Group conversions defined for this type parameter.
	 */
	@Immutable
	private final Map<Class<?>, Class<?>> groupConversions;

	/**
	 * Whether any container element (it can be nested) is marked for cascaded validation.
	 */
	private final boolean hasContainerElementsMarkedForCascading;

	/**
	 * Whether the constrained element has directly or indirectly (via type arguments) group conversions defined.
	 */
	private final boolean hasGroupConversionsOnAnnotatedObjectOrContainerElements;

	public CascadingMetaDataBuilder(Type enclosingType, TypeVariable<?> typeParameter, boolean cascading,
			Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData, Map<Class<?>, Class<?>> groupConversions) {
		this( enclosingType, typeParameter,
				TypeVariables.getContainerClass( typeParameter ), TypeVariables.getActualTypeParameter( typeParameter ),
				cascading, containerElementTypesCascadingMetaData, groupConversions );
	}

	private CascadingMetaDataBuilder(Type enclosingType, TypeVariable<?> typeParameter, Class<?> declaredContainerClass, TypeVariable<?> declaredTypeParameter,
			boolean cascading, Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData,
			Map<Class<?>, Class<?>> groupConversions) {
		this.enclosingType = enclosingType;
		this.typeParameter = typeParameter;
		this.declaredContainerClass = declaredContainerClass;
		this.declaredTypeParameter = declaredTypeParameter;
		this.cascading = cascading;
		this.groupConversions = CollectionHelper.toImmutableMap( groupConversions );
		this.containerElementTypesCascadingMetaData = CollectionHelper.toImmutableMap( containerElementTypesCascadingMetaData );

		boolean tmpHasContainerElementsMarkedForCascading = false;
		boolean tmpHasGroupConversionsOnAnnotatedObjectOrContainerElements = !groupConversions.isEmpty();
		for ( CascadingMetaDataBuilder nestedCascadingTypeParameter : containerElementTypesCascadingMetaData.values() ) {
			tmpHasContainerElementsMarkedForCascading = tmpHasContainerElementsMarkedForCascading
					|| nestedCascadingTypeParameter.cascading || nestedCascadingTypeParameter.hasContainerElementsMarkedForCascading;
			tmpHasGroupConversionsOnAnnotatedObjectOrContainerElements = tmpHasGroupConversionsOnAnnotatedObjectOrContainerElements
					|| nestedCascadingTypeParameter.hasGroupConversionsOnAnnotatedObjectOrContainerElements;
		}
		hasContainerElementsMarkedForCascading = tmpHasContainerElementsMarkedForCascading;
		hasGroupConversionsOnAnnotatedObjectOrContainerElements = tmpHasGroupConversionsOnAnnotatedObjectOrContainerElements;
	}

	public static CascadingMetaDataBuilder nonCascading() {
		return NON_CASCADING;
	}

	public static CascadingMetaDataBuilder annotatedObject(Type cascadableType, boolean cascading, Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData, Map<Class<?>, Class<?>> groupConversions) {
		return new CascadingMetaDataBuilder( cascadableType, AnnotatedObject.INSTANCE, cascading, containerElementTypesCascadingMetaData, groupConversions );
	}

	public TypeVariable<?> getTypeParameter() {
		return typeParameter;
	}

	public Type getEnclosingType() {
		return enclosingType;
	}

	public Class<?> getDeclaredContainerClass() {
		return declaredContainerClass;
	}

	public TypeVariable<?> getDeclaredTypeParameter() {
		return declaredTypeParameter;
	}

	public boolean isCascading() {
		return cascading;
	}

	public Map<Class<?>, Class<?>> getGroupConversions() {
		return groupConversions;
	}

	public boolean hasContainerElementsMarkedForCascading() {
		return hasContainerElementsMarkedForCascading;
	}

	public boolean isMarkedForCascadingOnAnnotatedObjectOrContainerElements() {
		return cascading || hasContainerElementsMarkedForCascading;
	}

	public boolean hasGroupConversionsOnAnnotatedObjectOrContainerElements() {
		return hasGroupConversionsOnAnnotatedObjectOrContainerElements;
	}

	public Map<TypeVariable<?>, CascadingMetaDataBuilder> getContainerElementTypesCascadingMetaData() {
		return containerElementTypesCascadingMetaData;
	}

	public CascadingMetaDataBuilder merge(CascadingMetaDataBuilder otherCascadingTypeParameter) {
		if ( this == NON_CASCADING ) {
			return otherCascadingTypeParameter;
		}
		if ( otherCascadingTypeParameter == NON_CASCADING ) {
			return this;
		}

		boolean cascading = this.cascading || otherCascadingTypeParameter.cascading;

		Map<Class<?>, Class<?>> groupConversions = mergeGroupConversion( this.groupConversions, otherCascadingTypeParameter.groupConversions );

		Map<TypeVariable<?>, CascadingMetaDataBuilder> nestedCascadingTypeParameterMap = Stream
				.concat( this.containerElementTypesCascadingMetaData.entrySet().stream(),
						otherCascadingTypeParameter.containerElementTypesCascadingMetaData.entrySet().stream() )
				.collect(
						Collectors.toMap( entry -> entry.getKey(), entry -> entry.getValue(), ( value1, value2 ) -> value1.merge( value2 ) ) );

		return new CascadingMetaDataBuilder( this.enclosingType, this.typeParameter, cascading, nestedCascadingTypeParameterMap, groupConversions );
	}

	public CascadingMetaData build(ValueExtractorManager valueExtractorManager, Object context) {
		validateGroupConversions( context );

		// we are 100% sure that we have a container and its elements are marked for cascading
		if ( !containerElementTypesCascadingMetaData.isEmpty() && hasContainerElementsMarkedForCascading ) {
			return ContainerCascadingMetaData.of( valueExtractorManager, this, context );
		}

		if ( cascading ) {
			// Now we are at the case when we might have something like @Valid SomeType something;
			//
			// First we should check if there are any available VE that can be applied to the current declared type (SomeType)
			// If such are found we promote the cascading to the type variable that we have VE for.
			// In case of the Map, call to valueExtractorManager.getPossibleValueExtractorCandidatesForCascadedValidation()
			// will not return MapKeyExtractor, hence by default it will only return MapValueExtractor which matches BV Spec.
			//
			// If we found more than one value extractor we throw an exception.
			Set<ValueExtractorDescriptor> possibleValueExtractorCandidates = valueExtractorManager.getPossibleValueExtractorCandidatesForCascadedValidation( enclosingType );
			if ( !possibleValueExtractorCandidates.isEmpty() ) {
				if ( possibleValueExtractorCandidates.size() > 1 ) {
					throw LOG.getUnableToGetMostSpecificValueExtractorDueToSeveralMaximallySpecificValueExtractorsDeclaredException(
							ReflectionHelper.getClassFromType( enclosingType ),
							ValueExtractorHelper.toValueExtractorClasses( possibleValueExtractorCandidates )
					);
				}
				return ContainerCascadingMetaData.of(
						valueExtractorManager,
						new CascadingMetaDataBuilder(
								enclosingType,
								typeParameter,
								cascading,
								addCascadingMetaDataBasedOnValueExtractor( enclosingType, containerElementTypesCascadingMetaData, groupConversions, possibleValueExtractorCandidates.iterator().next() ),
								groupConversions
						),
						context
				);
			}

			// If there are no possible VEs that can be applied right away to a declared type we should check if
			// there are any VEs that can be potentially applied to our type at runtime. This should cover cases
			// like @Valid Object object; or @Valid ContainerWithoutRegisteredVE container; where at runtime we can have
			// object = new ArrayList<>(); or container = new ContainerWithRegisteredVE(); (ContainerWithRegisteredVE extends ContainerWithoutRegisteredVE)
			// so we are looking for VEs such that ValueExtractorDescriptor#getContainerType() is assignable to declared type under
			// inspection.
			Set<ValueExtractorDescriptor> potentialValueExtractorCandidates = valueExtractorManager.getPotentialValueExtractorCandidatesForCascadedValidation( enclosingType );

			// if such VEs were found we return an instance of PotentiallyContainerCascadingMetaData that will store those potential VEs
			// and they will be used at runtime to check if any of those could be applied to a runtime type and if PotentiallyContainerCascadingMetaData
			// should be promoted to ContainerCascadingMetaData or not.
			if ( !potentialValueExtractorCandidates.isEmpty() ) {
				return PotentiallyContainerCascadingMetaData.of( this, potentialValueExtractorCandidates, context );
			}
		}

		// if cascading == false, or none of the above cases matched we just return a non container metadata
		return NonContainerCascadingMetaData.of( this, context );
	}

	private void validateGroupConversions(Object context) {
		// group conversions may only be configured for cascadable elements
		if ( !cascading && !groupConversions.isEmpty() ) {
			throw LOG.getGroupConversionOnNonCascadingElementException( context );
		}

		// group conversions may not be configured using a sequence as source
		for ( Class<?> group : groupConversions.keySet() ) {
			if ( group.isAnnotationPresent( GroupSequence.class ) ) {
				throw LOG.getGroupConversionForSequenceException( group );
			}
		}

		for ( CascadingMetaDataBuilder containerElementCascadingTypeParameter : containerElementTypesCascadingMetaData.values() ) {
			containerElementCascadingTypeParameter.validateGroupConversions( context );
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() );
		sb.append( " [" );
		sb.append( "enclosingType=" ).append( StringHelper.toShortString( enclosingType ) ).append( ", " );
		sb.append( "typeParameter=" ).append( typeParameter ).append( ", " );
		sb.append( "cascading=" ).append( cascading ).append( ", " );
		sb.append( "groupConversions=" ).append( groupConversions ).append( ", " );
		sb.append( "containerElementTypesCascadingMetaData=" ).append( containerElementTypesCascadingMetaData );
		sb.append( "]" );
		return sb.toString();
	}

	@Override
	public int hashCode() {
		// enclosingType is excluded from the hashCode and equals methods as it will not work for parameterized types
		// see TypeAnnotationDefinedOnAGenericTypeArgumentTest.constraintOnGenericTypeArgumentOfListReturnValueThrowsException for instance
		final int prime = 31;
		int result = 1;
		result = prime * result + typeParameter.hashCode();
		result = prime * result + ( cascading ? 1 : 0 );
		result = prime * result + groupConversions.hashCode();
		result = prime * result + containerElementTypesCascadingMetaData.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		// enclosingType is excluded from the hashCode and equals methods as it will not work for parameterized types
		// see TypeAnnotationDefinedOnAGenericTypeArgumentTest.constraintOnGenericTypeArgumentOfListReturnValueThrowsException for instance
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		CascadingMetaDataBuilder other = (CascadingMetaDataBuilder) obj;
		if ( !typeParameter.equals( other.typeParameter ) ) {
			return false;
		}
		if ( cascading != other.cascading ) {
			return false;
		}
		if ( !groupConversions.equals( other.groupConversions ) ) {
			return false;
		}
		if ( !containerElementTypesCascadingMetaData.equals( other.containerElementTypesCascadingMetaData ) ) {
			return false;
		}
		return true;
	}

	private static Map<Class<?>, Class<?>> mergeGroupConversion(Map<Class<?>, Class<?>> groupConversions, Map<Class<?>, Class<?>> otherGroupConversions) {
		if ( groupConversions.isEmpty() && otherGroupConversions.isEmpty() ) {
			// this is a rather common case so let's optimize it
			return Collections.emptyMap();
		}

		Map<Class<?>, Class<?>> mergedGroupConversions = new HashMap<>( groupConversions.size() + otherGroupConversions.size() );

		for ( Entry<Class<?>, Class<?>> otherGroupConversionEntry : otherGroupConversions.entrySet() ) {
			if ( groupConversions.containsKey( otherGroupConversionEntry.getKey() ) ) {
				throw LOG.getMultipleGroupConversionsForSameSourceException(
						otherGroupConversionEntry.getKey(),
						CollectionHelper.<Class<?>>asSet(
								groupConversions.get( otherGroupConversionEntry.getKey() ),
								otherGroupConversionEntry.getValue() ) );
			}
		}

		mergedGroupConversions.putAll( groupConversions );
		mergedGroupConversions.putAll( otherGroupConversions );

		return mergedGroupConversions;
	}

	private static Map<TypeVariable<?>, CascadingMetaDataBuilder> addCascadingMetaDataBasedOnValueExtractor(Type cascadableType, Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData, Map<Class<?>, Class<?>> groupConversions,
			 ValueExtractorDescriptor possibleValueExtractor) {
		Class<?> cascadableClass = ReflectionHelper.getClassFromType( cascadableType );
		if ( cascadableClass.isArray() ) {
			// for arrays, we need to add an ArrayElement cascading metadata: it's the only way arrays support cascading at the moment.
			return addArrayElementCascadingMetaData( cascadableClass, containerElementTypesCascadingMetaData, groupConversions );
		}
		else {
			Map<TypeVariable<?>, CascadingMetaDataBuilder> cascadingMetaData = containerElementTypesCascadingMetaData;
				cascadingMetaData = addCascadingMetaData(
						cascadableClass,
						possibleValueExtractor.getContainerType(),
						possibleValueExtractor.getExtractedTypeParameter(),
						cascadingMetaData,
						groupConversions
				);
			return cascadingMetaData;
		}
	}

	private static Map<TypeVariable<?>, CascadingMetaDataBuilder> addCascadingMetaData(final Class<?> enclosingType, Class<?> referenceType,
			TypeVariable<?> typeParameter, Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData,
			Map<Class<?>, Class<?>> groupConversions) {
		// we try to find a corresponding type parameter in the current cascadable type
		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> typeVariableBindings = TypeVariableBindings.getTypeVariableBindings( enclosingType );
		final TypeVariable<?> correspondingTypeParameter = typeVariableBindings.get( referenceType ).entrySet().stream()
				.filter( e -> Objects.equals( e.getKey().getGenericDeclaration(), enclosingType ) )
				.collect( Collectors.toMap( Map.Entry::getValue, Map.Entry::getKey ) )
				.get( typeParameter );

		Class<?> cascadableClass;
		TypeVariable<?> cascadableTypeParameter;
		if ( correspondingTypeParameter != null ) {
			cascadableClass = enclosingType;
			cascadableTypeParameter = correspondingTypeParameter;
		}
		else {
			// if we can't find one, we default to the reference type (e.g. List.class for instance)
			cascadableClass = referenceType;
			cascadableTypeParameter = typeParameter;
		}

		Map<TypeVariable<?>, CascadingMetaDataBuilder> amendedCascadingMetadata = CollectionHelper.newHashMap( containerElementTypesCascadingMetaData.size() + 1 );
		amendedCascadingMetadata.putAll( containerElementTypesCascadingMetaData );

		if ( containerElementTypesCascadingMetaData.containsKey( cascadableTypeParameter ) ) {
			amendedCascadingMetadata.put( cascadableTypeParameter,
					makeCascading( containerElementTypesCascadingMetaData.get( cascadableTypeParameter ), groupConversions ) );
		}
		else {
			amendedCascadingMetadata.put( cascadableTypeParameter,
					new CascadingMetaDataBuilder( cascadableClass, cascadableTypeParameter, enclosingType, correspondingTypeParameter, true,
							Collections.emptyMap(), groupConversions ) );
		}

		return amendedCascadingMetadata;
	}

	private static Map<TypeVariable<?>, CascadingMetaDataBuilder> addArrayElementCascadingMetaData(final Class<?> enclosingType,
			Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData,
			Map<Class<?>, Class<?>> groupConversions) {
		Map<TypeVariable<?>, CascadingMetaDataBuilder> amendedCascadingMetadata = CollectionHelper.newHashMap( containerElementTypesCascadingMetaData.size() + 1 );
		amendedCascadingMetadata.putAll( containerElementTypesCascadingMetaData );

		TypeVariable<?> cascadableTypeParameter = new ArrayElement( enclosingType );

		amendedCascadingMetadata.put( cascadableTypeParameter,
				new CascadingMetaDataBuilder( enclosingType, cascadableTypeParameter, true, Collections.emptyMap(), groupConversions ) );

		return amendedCascadingMetadata;
	}

	private static CascadingMetaDataBuilder makeCascading(CascadingMetaDataBuilder cascadingTypeParameter, Map<Class<?>, Class<?>> groupConversions) {
		return new CascadingMetaDataBuilder( cascadingTypeParameter.enclosingType, cascadingTypeParameter.typeParameter, true,
				cascadingTypeParameter.containerElementTypesCascadingMetaData,
				cascadingTypeParameter.groupConversions.isEmpty() ? groupConversions : cascadingTypeParameter.groupConversions );
	}
}
