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
import java.util.Set;

import jakarta.validation.metadata.GroupConversionDescriptor;

import org.hibernate.validator.internal.engine.valueextraction.AnnotatedObject;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A simplified view of the cascading validation metadata for a potentially container element at runtime.
 * Has a set of possible {@link ValueExtractorDescriptor}s that might be applied to a potential runtime type.
 *
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class PotentiallyContainerCascadingMetaData implements CascadingMetaData {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * The group conversions defined for this type parameter.
	 */
	private final GroupConversionHelper groupConversionHelper;

	private final Set<ValueExtractorDescriptor> potentialValueExtractorDescriptors;

	public static PotentiallyContainerCascadingMetaData of(CascadingMetaDataBuilder cascadingMetaDataBuilder, Set<ValueExtractorDescriptor> potentialValueExtractorDescriptors, Object context) {
		return new PotentiallyContainerCascadingMetaData( cascadingMetaDataBuilder, potentialValueExtractorDescriptors );
	}

	private PotentiallyContainerCascadingMetaData(CascadingMetaDataBuilder cascadingMetaDataBuilder, Set<ValueExtractorDescriptor> potentialValueExtractorDescriptors) {
		this( potentialValueExtractorDescriptors, GroupConversionHelper.of( cascadingMetaDataBuilder.getGroupConversions() ) );
	}

	private PotentiallyContainerCascadingMetaData(Set<ValueExtractorDescriptor> potentialValueExtractorDescriptors, GroupConversionHelper groupConversionHelper) {
		this.potentialValueExtractorDescriptors = potentialValueExtractorDescriptors;
		this.groupConversionHelper = groupConversionHelper;
	}

	@Override
	public TypeVariable<?> getTypeParameter() {
		return AnnotatedObject.INSTANCE;
	}

	@Override
	public boolean isCascading() {
		return true;
	}

	@Override
	public boolean isMarkedForCascadingOnAnnotatedObjectOrContainerElements() {
		return true;
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
	public CascadingMetaData addRuntimeContainerSupport(ValueExtractorManager valueExtractorManager, Class<?> valueClass) {
		ValueExtractorDescriptor compliantValueExtractor = valueExtractorManager.getResolver()
				.getMaximallySpecificValueExtractorForAllContainerElements( valueClass, potentialValueExtractorDescriptors );
		if ( compliantValueExtractor == null ) {
			return this;
		}

		return new ContainerCascadingMetaData(
				valueClass,
				Collections.singletonList(
						new ContainerCascadingMetaData(
								compliantValueExtractor.getContainerType(),
								compliantValueExtractor.getExtractedTypeParameter(),
								compliantValueExtractor.getContainerType(),
								compliantValueExtractor.getExtractedTypeParameter(),
								groupConversionHelper.isEmpty() ? GroupConversionHelper.EMPTY : groupConversionHelper
						)
				),
				groupConversionHelper,
				Collections.singleton( compliantValueExtractor )
		);
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
		sb.append( "groupConversions=" ).append( groupConversionHelper ).append( ", " );
		sb.append( "]" );
		return sb.toString();
	}
}
