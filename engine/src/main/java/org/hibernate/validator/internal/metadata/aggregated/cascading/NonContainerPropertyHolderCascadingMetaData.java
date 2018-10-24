/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated.cascading;

import java.util.Map;

import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;
import org.hibernate.validator.internal.metadata.manager.ConstraintMetaDataManager;
import org.hibernate.validator.internal.util.Contracts;

/**
 * Extended view of non container cascading metadta for property holders that in addition stores mapping name.
 *
 * @author Marko Bekhta
 */
public class NonContainerPropertyHolderCascadingMetaData extends NonContainerCascadingMetaData {

	/**
	 * Name of the constraint mappings to be applied.
	 */
	private final String mapping;

	public static NonContainerPropertyHolderCascadingMetaData of(String mapping, Map<Class<?>, Class<?>> groupConversions) {
		Contracts.assertNotEmpty( mapping, "Property holder mapping cannot be an empty string." );

		return new NonContainerPropertyHolderCascadingMetaData( mapping, groupConversions );
	}

	private NonContainerPropertyHolderCascadingMetaData(String mapping, Map<Class<?>, Class<?>> groupConversions) {
		super( true, GroupConversionHelper.of( groupConversions ) );
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
		sb.append( "cascading=" ).append( isCascading() ).append( ", " );
		sb.append( "]" );
		return sb.toString();
	}
}
