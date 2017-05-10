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
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.validator.internal.engine.cascading.AnnotatedObject;
import org.hibernate.validator.internal.engine.cascading.ArrayElement;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
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

	/**
	 * The enclosing type that defines this type parameter.
	 */
	private final Type enclosingType;

	/**
	 * The type parameter.
	 */
	private final TypeVariable<?> typeParameter;

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
		this.enclosingType = enclosingType;
		this.typeParameter = typeParameter;
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

	public static CascadingTypeParameter nonCascading(Type cascadableType) {
		// as it's non cascading anyway, we can also treat the arrays as annotated object
		return annotatedObject( cascadableType, false, Collections.emptyMap(), Collections.emptyMap() );
	}

	public static CascadingTypeParameter annotatedObject(Type cascadableType, boolean cascading,
			Map<TypeVariable<?>, CascadingTypeParameter> containerElementTypesCascadingMetaData, Map<Class<?>, Class<?>> groupConversions) {
		return new CascadingTypeParameter( cascadableType, AnnotatedObject.INSTANCE, cascading, containerElementTypesCascadingMetaData, groupConversions );
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
		boolean cascading = this.cascading || otherCascadingTypeParameter.cascading;

		Map<Class<?>, Class<?>> groupConversions = mergeGroupConversion( this.groupConversions, otherCascadingTypeParameter.groupConversions );

		Map<TypeVariable<?>, CascadingTypeParameter> nestedCascadingTypeParameterMap = Stream
				.concat( this.containerElementTypesCascadingMetaData.entrySet().stream(),
						otherCascadingTypeParameter.containerElementTypesCascadingMetaData.entrySet().stream() )
				.collect(
						Collectors.toMap( entry -> entry.getKey(), entry -> entry.getValue(), ( value1, value2 ) -> value1.merge( value2 ) ) );

		return new CascadingTypeParameter( this.enclosingType, this.typeParameter, cascading, nestedCascadingTypeParameterMap, groupConversions );
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
}
