/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated.cascading;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Marko Bekhta
 */
public class PropertyHolderCascadingMetaDataBuilder extends SimpleBeanCascadingMetaDataBuilder {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final String mapping;

	public PropertyHolderCascadingMetaDataBuilder(
			Type enclosingType,
			String mapping,
			TypeVariable<?> typeParameter,
			boolean cascading,
			Map<Class<?>, Class<?>> groupConversions) {
		super( enclosingType, typeParameter, cascading, Collections.emptyList(), groupConversions );
		this.mapping = mapping;
	}

	public PropertyHolderCascadingMetaDataBuilder(
			Type enclosingType,
			String mapping,
			TypeVariable<?> typeParameter,
			boolean cascading,
			Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData,
			Map<Class<?>, Class<?>> groupConversions) {
		super( enclosingType, typeParameter, cascading, containerElementTypesCascadingMetaData, groupConversions );
		this.mapping = mapping;
	}

	public static PropertyHolderCascadingMetaDataBuilder simplePropertyHolder(
			String mappingName,
			boolean cascading,
			Map<Class<?>, Class<?>> groupConversions) {
		return new PropertyHolderCascadingMetaDataBuilder( null, mappingName, AnnotatedObject.INSTANCE, cascading, groupConversions );
	}

	public static PropertyHolderCascadingMetaDataBuilder propertyHolderContainer(
			boolean cascading,
			Class<?> enclosingType,
			Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData,
			Map<Class<?>, Class<?>> groupConversions) {
		return new PropertyHolderCascadingMetaDataBuilder(
				enclosingType,
				null,
				AnnotatedObject.INSTANCE,
				cascading,
				containerElementTypesCascadingMetaData,
				groupConversions );
	}

	public static PropertyHolderCascadingMetaDataBuilder propertyHolderContainer(
			String mapping,
			boolean cascading,
			Class<?> declaredContainerClass,
			TypeVariable<?> declaredTypeVariable,
			Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData,
			Map<Class<?>, Class<?>> groupConversions) {
		return new PropertyHolderCascadingMetaDataBuilder(
				declaredContainerClass,
				mapping,
				declaredTypeVariable,
				cascading,
				containerElementTypesCascadingMetaData,
				groupConversions
		);
	}

	@Override
	public CascadingMetaDataBuilder merge(CascadingMetaDataBuilder otherCascadingTypeParameter) {
		if ( otherCascadingTypeParameter == NonCascadingMetaDataBuilder.INSTANCE ) {
			return this;
		}

		boolean cascading = this.cascading || otherCascadingTypeParameter.isCascading();

		Map<Class<?>, Class<?>> groupConversions = mergeGroupConversion( this.groupConversions, otherCascadingTypeParameter.getGroupConversions() );

		Map<TypeVariable<?>, CascadingMetaDataBuilder> nestedCascadingTypeParameterMap = Stream
				.concat(
						this.containerElementTypesCascadingMetaData.entrySet().stream(),
						otherCascadingTypeParameter.getContainerElementTypesCascadingMetaData().entrySet().stream() )
				.collect(
						Collectors.toMap( entry -> entry.getKey(), entry -> entry.getValue(), (value1, value2) -> value1.merge( value2 ) ) );

		return new SimpleBeanCascadingMetaDataBuilder( this.enclosingType, this.typeParameter, cascading, nestedCascadingTypeParameterMap, groupConversions );
	}

	@Override
	protected Set<ValueExtractorDescriptor> findContainerDetectionValueExtractorCandidates(ValueExtractorManager valueExtractorManager) {
		return mapping != null ? Collections.emptySet() : super.findContainerDetectionValueExtractorCandidates( valueExtractorManager );
	}

	@Override
	protected Set<ValueExtractorDescriptor> findPotentialValueExtractorCandidates(ValueExtractorManager valueExtractorManager) {
		return Collections.emptySet();
	}

	@Override
	protected NonContainerCascadingMetaData nonContainerCascadingMetaData() {
		return mapping != null ? NonContainerPropertyHolderCascadingMetaData.of( mapping, groupConversions ) : super.nonContainerCascadingMetaData();
	}

	@Override
	protected ContainerCascadingMetaData toContainerCascadingMetaData(ValueExtractorManager valueExtractorManager) {
		if ( mapping == null ) {
			return super.toContainerCascadingMetaData( valueExtractorManager );
		}
		return new ContainerPropertyHolderCascadingMetaData(
				valueExtractorManager,
				mapping,
				enclosingType,
				typeParameter,
				declaredContainerClass,
				declaredTypeParameter,
				containerElementTypesCascadingMetaData.entrySet().stream()
						.map( entry -> toContainerCascadingMetaData( entry.getValue(), valueExtractorManager ) )
						.collect( Collectors.collectingAndThen( Collectors.toList(), CollectionHelper::toImmutableList ) ),
				cascading,
				GroupConversionHelper.of( groupConversions ),
				isMarkedForCascadingOnAnnotatedObjectOrContainerElements()
		);
	}
}
