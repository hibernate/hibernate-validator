/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated.cascading;

import java.util.Set;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.util.Contracts;

/**
 * Extended view of potentially container cascading metadta for property holders that in addition stores mapping name.
 *
 * @author Marko Bekhta
 */
public class PotentiallyContainerPropertyHolderCascadingMetaData extends PotentiallyContainerCascadingMetaData {

	/**
	 * Name of the constraint mappings to be applied.
	 */
	private final String mapping;

	public static PotentiallyContainerPropertyHolderCascadingMetaData of(CascadingMetaDataBuilder cascadingMetaDataBuilder, Set<ValueExtractorDescriptor> potentialValueExtractorDescriptors, Object context) {
		Contracts.assertNotEmpty( cascadingMetaDataBuilder.getMappingName(), "Property holder mapping cannot be an empty string." );

		return new PotentiallyContainerPropertyHolderCascadingMetaData( cascadingMetaDataBuilder, potentialValueExtractorDescriptors );
	}

	private PotentiallyContainerPropertyHolderCascadingMetaData(CascadingMetaDataBuilder cascadingMetaDataBuilder, Set<ValueExtractorDescriptor> potentialValueExtractorDescriptors) {
		super( cascadingMetaDataBuilder, potentialValueExtractorDescriptors );
		this.mapping = cascadingMetaDataBuilder.getMappingName();
	}

	@Override
	protected ContainerCascadingMetaData createInnerMetadata(ValueExtractorDescriptor compliantValueExtractor, GroupConversionHelper groupConversionHelper) {
		return new ContainerPropertyHolderCascadingMetaData(
				mapping,
				compliantValueExtractor.getContainerType(),
				compliantValueExtractor.getExtractedTypeParameter(),
				compliantValueExtractor.getContainerType(),
				compliantValueExtractor.getExtractedTypeParameter(),
				groupConversionHelper.isEmpty() ? GroupConversionHelper.EMPTY : groupConversionHelper
		);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() );
		sb.append( " [" );
		sb.append( "mapping=" ).append( mapping ).append( ", " );
		sb.append( "]" );
		return sb.toString();
	}
}
