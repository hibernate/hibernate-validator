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

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.manager.ConstraintMetaDataManager;
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

	protected ContainerPropertyHolderCascadingMetaData(ValueExtractorManager valueExtractorManager, String mapping, Type enclosingType, TypeVariable<?> typeParameter,
			Class<?> declaredContainerClass, TypeVariable<?> declaredTypeParameter, List<ContainerCascadingMetaData> containerElementTypesCascadingMetaData,
			boolean cascading, GroupConversionHelper groupConversionHelper, boolean markedForCascadingOnContainerElements) {
		super( valueExtractorManager,
				enclosingType,
				typeParameter,
				declaredContainerClass,
				declaredTypeParameter,
				containerElementTypesCascadingMetaData,
				cascading, groupConversionHelper, markedForCascadingOnContainerElements );
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
