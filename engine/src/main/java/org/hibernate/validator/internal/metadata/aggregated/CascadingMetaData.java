/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.engine.cascading.AnnotatedObject;
import org.hibernate.validator.internal.engine.cascading.ArrayElement;
import org.hibernate.validator.internal.metadata.cascading.CascadingTypeParameter;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * An aggregated view of the cascading validation metadata. Note that it also includes the cascading validation metadata
 * defined on the root element via the {@link ArrayElement} and {@link AnnotatedObject} pseudo type parameters.
 *
 * @author Guillaume Smet
 */
public class CascadingMetaData {

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
	private final List<CascadingMetaData> containerElementTypesCascadingMetaData;

	/**
	 * If this type parameter is marked for cascading.
	 */
	private final boolean cascading;

	/**
	 * The group conversions defined for this type parameter.
	 */
	private GroupConversionHelper groupConversionHelper;

	/**
	 * Whether the constrained element is directly or indirectly (via type arguments) marked for cascaded validation.
	 */
	private final boolean markedForCascadingOnElementOrContainerElements;

	/**
	 * Whether the constrained element has directly or indirectly (via type arguments) group conversions defined.
	 */
	private final boolean hasGroupConversionsOnElementOrContainerElements;

	public CascadingMetaData(CascadingTypeParameter cascadingMetaData) {
		this.enclosingType = cascadingMetaData.getEnclosingType();
		this.typeParameter = cascadingMetaData.getTypeParameter();
		this.containerElementTypesCascadingMetaData = cascadingMetaData.getContainerElementTypesCascadingMetaData().entrySet().stream()
				.map( entry -> new CascadingMetaData( entry.getValue() ) )
				.collect( Collectors.collectingAndThen( Collectors.toList(), CollectionHelper::toImmutableList ) );
		this.groupConversionHelper = new GroupConversionHelper( cascadingMetaData.getGroupConversions() );
		this.cascading = cascadingMetaData.isCascading();
		this.markedForCascadingOnElementOrContainerElements = cascadingMetaData.isMarkedForCascadingOnElementOrContainerElements();
		this.hasGroupConversionsOnElementOrContainerElements = cascadingMetaData.isMarkedForCascadingOnElementOrContainerElements();
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

	public boolean isMarkedForCascadingOnElementOrContainerElements() {
		return markedForCascadingOnElementOrContainerElements;
	}

	public boolean hasGroupConversionsOnElementOrContainerElements() {
		return hasGroupConversionsOnElementOrContainerElements;
	}

	public List<CascadingMetaData> getContainerElementTypesCascadingMetaData() {
		return containerElementTypesCascadingMetaData;
	}

	public Class<?> convertGroup(Class<?> originalGroup) {
		return groupConversionHelper.convertGroup( originalGroup );
	}

	public Set<GroupConversionDescriptor> getGroupConversionDescriptors() {
		return groupConversionHelper.asDescriptors();
	}

	// FIXME GSM: probably better to move it to the constructor but we would need to pass the context to the constructor
	public void validateGroupConversions(String context) {
		groupConversionHelper.validateGroupConversions( cascading, context );
		for ( CascadingMetaData cascadingMetaData : containerElementTypesCascadingMetaData ) {
			cascadingMetaData.validateGroupConversions( context );
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
		sb.append( "groupConversions=" ).append( groupConversionHelper ).append( ", " );
		sb.append( "containerElementTypesCascadingMetaData=" ).append( containerElementTypesCascadingMetaData );
		sb.append( "]" );
		return sb.toString();
	}

}
