/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

import java.util.List;
import java.util.Set;

import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.propertyholder.PropertyHolderConfiguration;
import org.hibernate.validator.internal.metadata.raw.propertyholder.PropertyHolderConfigurationSource;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class PropertyHolderBeanMetaDataBuilder<T> {

	private final ConstraintHelper constraintHelper;
	private final ValidationOrderGenerator validationOrderGenerator;
	private final Class<T> propertyHolderClass;
	private final Set<BuilderDelegate> builders = newHashSet();
	private final TypeResolutionHelper typeResolutionHelper;
	private final ValueExtractorManager valueExtractorManager;

	private PropertyHolderConfigurationSource sequenceSource;
	private PropertyHolderConfigurationSource providerSource;
	private List<Class<?>> defaultGroupSequence;


	private PropertyHolderBeanMetaDataBuilder(
			ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager,
			ValidationOrderGenerator validationOrderGenerator,
			Class<T> propertyHolderClass) {
		this.propertyHolderClass = propertyHolderClass;
		this.constraintHelper = constraintHelper;
		this.validationOrderGenerator = validationOrderGenerator;
		this.typeResolutionHelper = typeResolutionHelper;
		this.valueExtractorManager = valueExtractorManager;
	}

	public static <T> PropertyHolderBeanMetaDataBuilder<T> getInstance(
			ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager,
			ValidationOrderGenerator validationOrderGenerator,
			Class<T> propertyHolderClass) {
		return new PropertyHolderBeanMetaDataBuilder<>(
				constraintHelper,
				typeResolutionHelper,
				valueExtractorManager,
				validationOrderGenerator,
				propertyHolderClass );
	}

	public void add(PropertyHolderConfiguration configuration) {
		if ( configuration.getDefaultGroupSequence() != null
				&& ( sequenceSource == null || configuration.getSource()
				.getPriority() >= sequenceSource.getPriority() ) ) {

			sequenceSource = configuration.getSource();
			defaultGroupSequence = configuration.getDefaultGroupSequence();
		}

		for ( ConstrainedElement constrainedElement : configuration.getConstrainedElements() ) {
			addMetaDataToBuilder( constrainedElement, builders );
		}
	}

	private void addMetaDataToBuilder(ConstrainedElement constrainableElement, Set<BuilderDelegate> builders) {
		for ( BuilderDelegate builder : builders ) {
			boolean foundBuilder = builder.add( constrainableElement );

			if ( foundBuilder ) {
				return;
			}
		}

		builders.add(
				new BuilderDelegate(
						propertyHolderClass,
						constrainableElement,
						constraintHelper,
						typeResolutionHelper,
						valueExtractorManager
				)
		);
	}

	public BeanMetaDataImpl<T> build() {
		Set<ConstraintMetaData> aggregatedElements = newHashSet();

		for ( BuilderDelegate builder : builders ) {
			aggregatedElements.addAll( builder.build() );
		}

		return new BeanMetaDataImpl<>(
				propertyHolderClass,
				defaultGroupSequence,
				null,
				aggregatedElements,
				validationOrderGenerator
		);
	}

	private static class BuilderDelegate {
		private final Class<?> propertyHolderClass;
		private final ConstrainedElement constrainedElement;
		private final ConstraintHelper constraintHelper;
		private final TypeResolutionHelper typeResolutionHelper;
		private final ValueExtractorManager valueExtractorManager;
		private MetaDataBuilder metaDataBuilder;
		private final int hashCode;

		public BuilderDelegate(
				Class<?> propertyHolderClass,
				ConstrainedElement constrainedElement,
				ConstraintHelper constraintHelper,
				TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager
		) {
			this.propertyHolderClass = propertyHolderClass;
			this.constrainedElement = constrainedElement;
			this.constraintHelper = constraintHelper;
			this.typeResolutionHelper = typeResolutionHelper;
			this.valueExtractorManager = valueExtractorManager;

			switch ( constrainedElement.getKind() ) {
				case FIELD:
					ConstrainedField constrainedField = (ConstrainedField) constrainedElement;
					metaDataBuilder = new PropertyMetaData.Builder(
							propertyHolderClass,
							constrainedField,
							constraintHelper,
							typeResolutionHelper,
							valueExtractorManager
					);
					break;
				default:
					throw new IllegalStateException(
							StringHelper.format( "Constrained element kind '%1$s' not supported here.", constrainedElement.getKind() ) );
			}

			this.hashCode = buildHashCode();
		}

		public boolean add(ConstrainedElement constrainedElement) {
			if ( metaDataBuilder != null && metaDataBuilder.accepts( constrainedElement ) ) {
				metaDataBuilder.add( constrainedElement );

				return true;
			}

			return false;
		}

		public Set<ConstraintMetaData> build() {
			Set<ConstraintMetaData> metaDataSet = newHashSet();

			if ( metaDataBuilder != null ) {
				metaDataSet.add( metaDataBuilder.build() );
			}

			return metaDataSet;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		private int buildHashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + propertyHolderClass.hashCode();
			result = prime * result + constrainedElement.hashCode();
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if ( this == obj ) {
				return true;
			}
			if ( !super.equals( obj ) ) {
				return false;
			}
			if ( getClass() != obj.getClass() ) {
				return false;
			}
			BuilderDelegate other = (BuilderDelegate) obj;
			if ( !propertyHolderClass.equals( other.propertyHolderClass ) ) {
				return false;
			}
			if ( !constrainedElement.equals( other.constrainedElement ) ) {
				return false;
			}
			return true;
		}
	}
}
