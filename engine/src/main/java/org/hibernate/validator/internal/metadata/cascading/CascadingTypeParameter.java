/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.cascading;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.GroupSequence;

import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaData;
import org.hibernate.validator.internal.metadata.aggregated.ContainerCascadingMetaData;
import org.hibernate.validator.internal.metadata.aggregated.NonContainerCascadingMetaData;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeVariableBindings;
import org.hibernate.validator.internal.util.TypeVariables;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * A type parameter that is marked for cascaded validation and/or has one or more nested type parameters marked for
 * cascaded validation.
 *
 * @author Guillaume Smet
 */
public class CascadingTypeParameter {

	private static final Log LOG = LoggerFactory.make();

	private static final CascadingTypeParameter NON_CASCADING =
			new CascadingTypeParameter( null, null, null, null, false, Collections.emptyMap(), Collections.emptyMap() );

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
	private final Map<TypeVariable<?>, CascadingTypeParameter> containerElementTypesCascadingMetaData;

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
	 * Whether the constrained element is directly or indirectly (via type arguments) marked for cascaded validation.
	 */
	private final boolean markedForCascadingOnElementOrContainerElements;

	/**
	 * Whether the constrained element has directly or indirectly (via type arguments) group conversions defined.
	 */
	private final boolean hasGroupConversionsOnElementOrContainerElements;

	public CascadingTypeParameter(Type enclosingType, TypeVariable<?> typeParameter, boolean cascading,
			Map<TypeVariable<?>, CascadingTypeParameter> containerElementTypesCascadingMetaData, Map<Class<?>, Class<?>> groupConversions) {
		this( enclosingType, typeParameter,
				TypeVariables.getContainerClass( typeParameter ), TypeVariables.getActualTypeParameter( typeParameter ),
				cascading, containerElementTypesCascadingMetaData, groupConversions );
	}

	private CascadingTypeParameter(Type enclosingType, TypeVariable<?> typeParameter, Class<?> declaredContainerClass, TypeVariable<?> declaredTypeParameter,
			boolean cascading, Map<TypeVariable<?>, CascadingTypeParameter> containerElementTypesCascadingMetaData,
			Map<Class<?>, Class<?>> groupConversions) {
		this.enclosingType = enclosingType;
		this.typeParameter = typeParameter;
		this.declaredContainerClass = declaredContainerClass;
		this.declaredTypeParameter = declaredTypeParameter;
		this.cascading = cascading;
		this.groupConversions = CollectionHelper.toImmutableMap( groupConversions );
		this.containerElementTypesCascadingMetaData = CollectionHelper.toImmutableMap( containerElementTypesCascadingMetaData );

		boolean tmpMarkedForCascadingOnElementOrContainerElements = cascading;
		boolean tmpHasGroupConversionsOnElementOrContainerElements = !groupConversions.isEmpty();
		for ( CascadingTypeParameter nestedCascadingTypeParameter : containerElementTypesCascadingMetaData.values() ) {
			tmpMarkedForCascadingOnElementOrContainerElements = tmpMarkedForCascadingOnElementOrContainerElements
					|| nestedCascadingTypeParameter.markedForCascadingOnElementOrContainerElements;
			tmpHasGroupConversionsOnElementOrContainerElements = tmpHasGroupConversionsOnElementOrContainerElements
					|| nestedCascadingTypeParameter.hasGroupConversionsOnElementOrContainerElements;
		}
		markedForCascadingOnElementOrContainerElements = tmpMarkedForCascadingOnElementOrContainerElements;
		hasGroupConversionsOnElementOrContainerElements = tmpHasGroupConversionsOnElementOrContainerElements;
	}

	public static CascadingTypeParameter nonCascading() {
		return NON_CASCADING;
	}

	public static CascadingTypeParameter annotatedObject(Type cascadableType, boolean cascading,
			Map<TypeVariable<?>, CascadingTypeParameter> containerElementTypesCascadingMetaData, Map<Class<?>, Class<?>> groupConversions) {
		Map<TypeVariable<?>, CascadingTypeParameter> amendedContainerElementsCascadingMetaData;
		if ( cascading ) {
			amendedContainerElementsCascadingMetaData = addPotentialLegacyCascadingMetaData( cascadableType, containerElementTypesCascadingMetaData,
					groupConversions );
		}
		else {
			amendedContainerElementsCascadingMetaData = containerElementTypesCascadingMetaData;
		}

		return new CascadingTypeParameter( cascadableType, AnnotatedObject.INSTANCE, cascading, amendedContainerElementsCascadingMetaData, groupConversions );
	}

	public static CascadingTypeParameter arrayElement(Type cascadableType, boolean cascading,
			Map<TypeVariable<?>, CascadingTypeParameter> containerElementTypesCascadingMetaData, Map<Class<?>, Class<?>> groupConversions) {
		return new CascadingTypeParameter( cascadableType, new ArrayElement( cascadableType ), cascading,
				containerElementTypesCascadingMetaData, groupConversions );
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

	public boolean isMarkedForCascadingOnElementOrContainerElements() {
		return markedForCascadingOnElementOrContainerElements;
	}

	public boolean hasGroupConversionsOnElementOrContainerElements() {
		return hasGroupConversionsOnElementOrContainerElements;
	}

	public Map<TypeVariable<?>, CascadingTypeParameter> getContainerElementTypesCascadingMetaData() {
		return containerElementTypesCascadingMetaData;
	}

	public CascadingTypeParameter merge(CascadingTypeParameter otherCascadingTypeParameter) {
		if ( this == NON_CASCADING ) {
			return otherCascadingTypeParameter;
		}
		if ( otherCascadingTypeParameter == NON_CASCADING ) {
			return this;
		}

		boolean cascading = this.cascading || otherCascadingTypeParameter.cascading;

		Map<Class<?>, Class<?>> groupConversions = mergeGroupConversion( this.groupConversions, otherCascadingTypeParameter.groupConversions );

		Map<TypeVariable<?>, CascadingTypeParameter> nestedCascadingTypeParameterMap = Stream
				.concat( this.containerElementTypesCascadingMetaData.entrySet().stream(),
						otherCascadingTypeParameter.containerElementTypesCascadingMetaData.entrySet().stream() )
				.collect(
						Collectors.toMap( entry -> entry.getKey(), entry -> entry.getValue(), ( value1, value2 ) -> value1.merge( value2 ) ) );

		return new CascadingTypeParameter( this.enclosingType, this.typeParameter, cascading, nestedCascadingTypeParameterMap, groupConversions );
	}

	public CascadingMetaData build(ValueExtractorManager valueExtractorManager, Object context) {
		validateGroupConversions( context );

		if ( containerElementTypesCascadingMetaData.isEmpty() ) {
			return NonContainerCascadingMetaData.of( this, context );
		}
		else {
			return ContainerCascadingMetaData.of( valueExtractorManager, this, context );
		}
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

		for ( CascadingTypeParameter containerElementCascadingTypeParameter : containerElementTypesCascadingMetaData.values() ) {
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
		CascadingTypeParameter other = (CascadingTypeParameter) obj;
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

	private static Map<TypeVariable<?>, CascadingTypeParameter> addPotentialLegacyCascadingMetaData(Type cascadableType,
			Map<TypeVariable<?>, CascadingTypeParameter> containerElementTypesCascadingMetaData, Map<Class<?>, Class<?>> groupConversions) {
		Class<?> cascadableClass = ReflectionHelper.getClassFromType( cascadableType );

		if ( Map.class.isAssignableFrom( cascadableClass ) ) {
			return addLegacyCascadingMetaData( cascadableClass, Map.class, 1, containerElementTypesCascadingMetaData, groupConversions );
		}
		else if ( List.class.isAssignableFrom( cascadableClass ) ) {
			return addLegacyCascadingMetaData( cascadableClass, List.class, 0, containerElementTypesCascadingMetaData, groupConversions );
		}
		else if ( Iterable.class.isAssignableFrom( cascadableClass ) ) {
			return addLegacyCascadingMetaData( cascadableClass, Iterable.class, 0, containerElementTypesCascadingMetaData, groupConversions );
		}
		else if ( Optional.class.isAssignableFrom( cascadableClass ) ) {
			return addLegacyCascadingMetaData( cascadableClass, Optional.class, 0, containerElementTypesCascadingMetaData, groupConversions );
		}
		else if ( cascadableClass.isArray() ) {
			// for arrays, we need to add an ArrayElement cascading metadata: it's the only way arrays support cascading at the moment.
			return addArrayElementCascadingMetaData( cascadableClass, containerElementTypesCascadingMetaData, groupConversions );
		}
		else {
			return containerElementTypesCascadingMetaData;
		}
	}

	private static Map<TypeVariable<?>, CascadingTypeParameter> addLegacyCascadingMetaData(final Class<?> enclosingType, Class<?> referenceType,
			int typeParameterIndex, Map<TypeVariable<?>, CascadingTypeParameter> containerElementTypesCascadingMetaData,
			Map<Class<?>, Class<?>> groupConversions) {
		// we try to find a corresponding type parameter in the current cascadable type
		Map<Class<?>, Map<TypeVariable<?>, TypeVariable<?>>> typeVariableBindings = TypeVariableBindings.getTypeVariableBindings( enclosingType );
		final TypeVariable<?> correspondingTypeParameter = typeVariableBindings.get( referenceType ).entrySet().stream()
				.filter( e -> Objects.equals( e.getKey().getGenericDeclaration(), enclosingType ) )
				.collect( Collectors.toMap( Map.Entry::getValue, Map.Entry::getKey ) )
				.get( referenceType.getTypeParameters()[typeParameterIndex] );

		Class<?> cascadableClass;
		TypeVariable<?> cascadableTypeParameter;
		if ( correspondingTypeParameter != null ) {
			cascadableClass = enclosingType;
			cascadableTypeParameter = correspondingTypeParameter;
		}
		else {
			// if we can't find one, we default to the reference type (e.g. List.class for instance)
			cascadableClass = referenceType;
			cascadableTypeParameter = referenceType.getTypeParameters()[typeParameterIndex];
		}

		Map<TypeVariable<?>, CascadingTypeParameter> amendedCascadingMetadata = CollectionHelper.newHashMap( containerElementTypesCascadingMetaData.size() + 1 );
		amendedCascadingMetadata.putAll( containerElementTypesCascadingMetaData );

		if ( containerElementTypesCascadingMetaData.containsKey( cascadableTypeParameter ) ) {
			amendedCascadingMetadata.put( cascadableTypeParameter,
					makeCascading( containerElementTypesCascadingMetaData.get( cascadableTypeParameter ), groupConversions ) );
		}
		else {
			amendedCascadingMetadata.put( cascadableTypeParameter,
					new CascadingTypeParameter( cascadableClass, cascadableTypeParameter, enclosingType, correspondingTypeParameter, true,
							Collections.emptyMap(), groupConversions ) );
		}

		return amendedCascadingMetadata;
	}

	private static Map<TypeVariable<?>, CascadingTypeParameter> addArrayElementCascadingMetaData(final Class<?> enclosingType,
			Map<TypeVariable<?>, CascadingTypeParameter> containerElementTypesCascadingMetaData,
			Map<Class<?>, Class<?>> groupConversions) {
		Map<TypeVariable<?>, CascadingTypeParameter> amendedCascadingMetadata = CollectionHelper.newHashMap( containerElementTypesCascadingMetaData.size() + 1 );
		amendedCascadingMetadata.putAll( containerElementTypesCascadingMetaData );

		TypeVariable<?> cascadableTypeParameter = new ArrayElement( enclosingType );

		amendedCascadingMetadata.put( cascadableTypeParameter,
				new CascadingTypeParameter( enclosingType, cascadableTypeParameter, true, Collections.emptyMap(), groupConversions ) );

		return amendedCascadingMetadata;
	}

	private static CascadingTypeParameter makeCascading(CascadingTypeParameter cascadingTypeParameter, Map<Class<?>, Class<?>> groupConversions) {
		return new CascadingTypeParameter( cascadingTypeParameter.enclosingType, cascadingTypeParameter.typeParameter, true,
				cascadingTypeParameter.containerElementTypesCascadingMetaData,
				cascadingTypeParameter.groupConversions.isEmpty() ? groupConversions : cascadingTypeParameter.groupConversions );
	}
}
