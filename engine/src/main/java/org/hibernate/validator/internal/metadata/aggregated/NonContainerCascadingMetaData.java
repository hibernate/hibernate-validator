/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.engine.valueextraction.LegacyCollectionSupportValueExtractors;
import org.hibernate.validator.internal.util.TypeVariables;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A simplified view of the cascading validation metadata for a non container element.
 *
 * @author Guillaume Smet
 */
public class NonContainerCascadingMetaData implements CascadingMetaData {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final NonContainerCascadingMetaData NON_CASCADING = new NonContainerCascadingMetaData( false,
			GroupConversionHelper.EMPTY );

	private static final NonContainerCascadingMetaData CASCADING_WITHOUT_GROUP_CONVERSIONS = new NonContainerCascadingMetaData( true,
			GroupConversionHelper.EMPTY );

	private static final ContainerCascadingMetaData LIST_CONTAINER_WITHOUT_GROUP_CONVERSIONS =
			new ContainerCascadingMetaData( List.class, List.class.getTypeParameters()[0], List.class, List.class.getTypeParameters()[0],
					GroupConversionHelper.EMPTY, LegacyCollectionSupportValueExtractors.LIST );

	private static final ContainerCascadingMetaData MAP_CONTAINER_WITHOUT_GROUP_CONVERSIONS =
			new ContainerCascadingMetaData( Map.class, Map.class.getTypeParameters()[1], Map.class, Map.class.getTypeParameters()[1],
					GroupConversionHelper.EMPTY, LegacyCollectionSupportValueExtractors.MAP );

	private static final ContainerCascadingMetaData ITERABLE_CONTAINER_WITHOUT_GROUP_CONVERSIONS =
			new ContainerCascadingMetaData( Iterable.class, Iterable.class.getTypeParameters()[0], Iterable.class, Iterable.class.getTypeParameters()[0],
					GroupConversionHelper.EMPTY, LegacyCollectionSupportValueExtractors.ITERABLE );

	private static final ContainerCascadingMetaData OPTIONAL_CONTAINER_WITHOUT_GROUP_CONVERSIONS =
			new ContainerCascadingMetaData( Optional.class, Optional.class.getTypeParameters()[0], Optional.class, Optional.class.getTypeParameters()[0],
					GroupConversionHelper.EMPTY, LegacyCollectionSupportValueExtractors.OPTIONAL );

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
	public CascadingMetaData addRuntimeLegacyCollectionSupport(Class<?> valueClass) {
		if ( !cascading ) {
			return this;
		}

		ContainerCascadingMetaData legacyContainerElementCascadingMetaData = getLegacyContainerElementCascadingMetaData( valueClass );
		if ( legacyContainerElementCascadingMetaData == null ) {
			return this;
		}

		return new ContainerCascadingMetaData( valueClass, Collections.singletonList( legacyContainerElementCascadingMetaData ), groupConversionHelper );
	}

	private ContainerCascadingMetaData getLegacyContainerElementCascadingMetaData(Class<?> valueClass) {
		if ( List.class.isAssignableFrom( valueClass ) ) {
			return groupConversionHelper.isEmpty() ?
					LIST_CONTAINER_WITHOUT_GROUP_CONVERSIONS :
					new ContainerCascadingMetaData( List.class, List.class.getTypeParameters()[0], List.class, List.class.getTypeParameters()[0],
					groupConversionHelper, LegacyCollectionSupportValueExtractors.LIST );
		}
		else if ( Map.class.isAssignableFrom( valueClass ) ) {
			return groupConversionHelper.isEmpty() ?
					MAP_CONTAINER_WITHOUT_GROUP_CONVERSIONS :
					new ContainerCascadingMetaData( Map.class, Map.class.getTypeParameters()[1], Map.class, Map.class.getTypeParameters()[1],
					groupConversionHelper, LegacyCollectionSupportValueExtractors.MAP );
		}
		else if ( Iterable.class.isAssignableFrom( valueClass ) ) {
			return groupConversionHelper.isEmpty() ?
					ITERABLE_CONTAINER_WITHOUT_GROUP_CONVERSIONS :
					new ContainerCascadingMetaData( Iterable.class, Iterable.class.getTypeParameters()[0], Iterable.class, Iterable.class.getTypeParameters()[0],
					groupConversionHelper, LegacyCollectionSupportValueExtractors.ITERABLE );
		}
		else if ( Optional.class.isAssignableFrom( valueClass ) ) {
			return groupConversionHelper.isEmpty() ?
					OPTIONAL_CONTAINER_WITHOUT_GROUP_CONVERSIONS :
					new ContainerCascadingMetaData( Optional.class, Optional.class.getTypeParameters()[0], Optional.class, Optional.class.getTypeParameters()[0],
					groupConversionHelper, LegacyCollectionSupportValueExtractors.OPTIONAL );
		}
		else if ( valueClass.isArray() ) {
			TypeVariable<?> typeParameter = new ArrayElement( valueClass );

			return new ContainerCascadingMetaData( valueClass, typeParameter,
					TypeVariables.getContainerClass( typeParameter ), TypeVariables.getActualTypeParameter( typeParameter ),
					groupConversionHelper, LegacyCollectionSupportValueExtractors.ARRAY );
		}

		return null;
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
