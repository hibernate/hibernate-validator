/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.Set;

import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A simplified view of the cascading validation metadata for a non container element.
 *
 * @author Guillaume Smet
 */
public class NonContainerCascadingMetaData implements CascadingMetaData {

	private static final Log LOG = LoggerFactory.make();

	private static final NonContainerCascadingMetaData NON_CASCADING = new NonContainerCascadingMetaData( false,
			GroupConversionHelper.of( Collections.emptyMap() ) );

	private static final NonContainerCascadingMetaData CASCADING_WITHOUT_GROUP_CONVERSIONS = new NonContainerCascadingMetaData( true,
			GroupConversionHelper.of( Collections.emptyMap() ) );

	/**
	 * If this type parameter is marked for cascading.
	 */
	private final boolean cascading;

	/**
	 * The group conversions defined for this type parameter.
	 */
	private GroupConversionHelper groupConversionHelper;

	public static NonContainerCascadingMetaData of(CascadingMetaDataBuilder cascadingMetaDataBuilder, Object context) {
		if ( !cascadingMetaDataBuilder.isCascading() ) {
			return NON_CASCADING;
		}
		else if ( cascadingMetaDataBuilder.getGroupConversions().isEmpty() ) {
			return CASCADING_WITHOUT_GROUP_CONVERSIONS;
		}
		else {
			return new NonContainerCascadingMetaData( cascadingMetaDataBuilder );
		}
	}

	private NonContainerCascadingMetaData(CascadingMetaDataBuilder cascadingMetaDataBuilder) {
		this(
				cascadingMetaDataBuilder.isCascading(),
				GroupConversionHelper.of( cascadingMetaDataBuilder.getGroupConversions() )
		);
	}

	private NonContainerCascadingMetaData(boolean cascading, GroupConversionHelper groupConversionHelper) {
		this.cascading = cascading;
		this.groupConversionHelper = groupConversionHelper;
	}

	@Override
	public TypeVariable<?> getTypeParameter() {
		return AnnotatedObject.INSTANCE;
	}

	@Override
	public boolean isCascading() {
		return cascading;
	}

	@Override
	public boolean isMarkedForCascadingOnAnnotatedObjectOrContainerElements() {
		return cascading;
	}

	@Override
	public Class<?> convertGroup(Class<?> originalGroup) {
		return groupConversionHelper.convertGroup( originalGroup );
	}

	@Override
	public Set<GroupConversionDescriptor> getGroupConversionDescriptors() {
		return groupConversionHelper.asDescriptors();
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends CascadingMetaData> T as(Class<T> clazz) {
		if ( clazz.isAssignableFrom( getClass() ) ) {
			return (T) this;
		}

		throw LOG.getUnableToCastException( this, clazz );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() );
		sb.append( " [" );
		sb.append( "cascading=" ).append( cascading ).append( ", " );
		sb.append( "groupConversions=" ).append( groupConversionHelper ).append( ", " );
		sb.append( "]" );
		return sb.toString();
	}
}
