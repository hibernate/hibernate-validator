/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated.cascading;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.manager.ConstraintMetaDataManager;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.StringHelper;

/**
 * Extended view of container cascading metadta for property holders that in addition stores mapping name.
 *
 * @author Marko Bekhta
 */
public class ContainerPropertyHolderCascadingMetaData extends ContainerCascadingMetaData {

	/**
	 * Name of the constraint mappings to be applied.
	 */
	private final String mapping;

	public static ContainerPropertyHolderCascadingMetaData of(ValueExtractorManager valueExtractorManager, CascadingMetaDataBuilder cascadingMetaDataBuilder,
			Object context) {
		Contracts.assertNotEmpty( cascadingMetaDataBuilder.getMappingName(), "Property holder mapping cannot be an empty string." );

		return new ContainerPropertyHolderCascadingMetaData( valueExtractorManager, cascadingMetaDataBuilder );
	}

	private ContainerPropertyHolderCascadingMetaData(ValueExtractorManager valueExtractorManager, CascadingMetaDataBuilder cascadingMetaDataBuilder) {
		super( valueExtractorManager, cascadingMetaDataBuilder );
		this.mapping = cascadingMetaDataBuilder.getMappingName();
	}

	ContainerPropertyHolderCascadingMetaData(String mapping, Type enclosingType, List<ContainerPropertyHolderCascadingMetaData> containerElementTypesCascadingMetaData,
			GroupConversionHelper groupConversionHelper, Set<ValueExtractorDescriptor> valueExtractorCandidates) {
		super( enclosingType, containerElementTypesCascadingMetaData, groupConversionHelper, valueExtractorCandidates );
		this.mapping = mapping;
	}

	ContainerPropertyHolderCascadingMetaData(String mapping, Type enclosingType, TypeVariable<?> typeParameter, Class<?> declaredContainerClass, TypeVariable<?> declaredTypeParameter,
			GroupConversionHelper groupConversionHelper) {
		super( enclosingType, typeParameter, declaredContainerClass, declaredTypeParameter, groupConversionHelper );
		this.mapping = mapping;
	}

	@Override
	public BeanMetaData<?> getBeanMetaDataForCascadable(ConstraintMetaDataManager constraintMetaDataManager, Object value) {
		return constraintMetaDataManager.getPropertyHolderBeanMetaData( value.getClass(), mapping );
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() );
		sb.append( " [" );
		sb.append( "mapping=" ).append( mapping ).append( ", " );
		sb.append( "enclosingType=" ).append( StringHelper.toShortString( getEnclosingType() ) ).append( ", " );
		sb.append( "typeParameter=" ).append( getTypeParameter() ).append( ", " );
		sb.append( "cascading=" ).append( isCascading() ).append( ", " );
		sb.append( "containerElementTypesCascadingMetaData=" ).append( getContainerElementTypesCascadingMetaData() );
		sb.append( "]" );
		return sb.toString();
	}
}
