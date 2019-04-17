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

import org.hibernate.validator.engine.HibernateConstrainedType;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.MethodValidationConfiguration;
import org.hibernate.validator.internal.engine.groups.ValidationOrderGenerator;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Chris Beckey &lt;cbeckey@paypal.com&gt;
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class BeanMetaDataBuilder<T> {

	private final ConstraintCreationContext constraintCreationContext;
	private final ValidationOrderGenerator validationOrderGenerator;
	private final HibernateConstrainedType<T> constrainedType;
	private final Set<BuilderDelegate> builders = newHashSet();
	private final ExecutableHelper executableHelper;
	private final ExecutableParameterNameProvider parameterNameProvider;
	private final MethodValidationConfiguration methodValidationConfiguration;

	private ConfigurationSource sequenceSource;
	private ConfigurationSource providerSource;
	private List<Class<?>> defaultGroupSequence;
	private DefaultGroupSequenceProvider<? super T> defaultGroupSequenceProvider;


	private BeanMetaDataBuilder(
			ConstraintCreationContext constraintCreationContext,
			ExecutableHelper executableHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			ValidationOrderGenerator validationOrderGenerator,
			HibernateConstrainedType<T> constrainedType,
			MethodValidationConfiguration methodValidationConfiguration) {
		this.constraintCreationContext = constraintCreationContext;
		this.validationOrderGenerator = validationOrderGenerator;
		this.executableHelper = executableHelper;
		this.parameterNameProvider = parameterNameProvider;
		this.constrainedType = constrainedType;
		this.methodValidationConfiguration = methodValidationConfiguration;
	}

	public static <T> BeanMetaDataBuilder<T> getInstance(
			ConstraintCreationContext constraintCreationContext,
			ExecutableHelper executableHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			ValidationOrderGenerator validationOrderGenerator,
			HibernateConstrainedType<T> constrainedType,
			MethodValidationConfiguration methodValidationConfiguration) {
		return new BeanMetaDataBuilder<>(
				constraintCreationContext,
				executableHelper,
				parameterNameProvider,
				validationOrderGenerator,
				constrainedType,
				methodValidationConfiguration );
	}

	public void add(BeanConfiguration<? super T> configuration) {
		if ( configuration.getConstrainedType().equals( constrainedType ) ) {
			if ( configuration.getDefaultGroupSequence() != null
					&& ( sequenceSource == null || configuration.getSource()
					.getPriority() >= sequenceSource.getPriority() ) ) {

				sequenceSource = configuration.getSource();
				defaultGroupSequence = configuration.getDefaultGroupSequence();
			}

			if ( configuration.getDefaultGroupSequenceProvider() != null
					&& ( providerSource == null || configuration.getSource()
					.getPriority() >= providerSource.getPriority() ) ) {

				providerSource = configuration.getSource();
				defaultGroupSequenceProvider = configuration.getDefaultGroupSequenceProvider();
			}
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
						constrainedType,
						constrainableElement,
						constraintCreationContext,
						executableHelper,
						parameterNameProvider,
						methodValidationConfiguration
				)
		);
	}

	public BeanMetaDataImpl<T> build(List<BeanMetaData<?>> beanMetadataHierarchy) {
		Set<ConstraintMetaData> aggregatedElements = newHashSet();

		for ( BuilderDelegate builder : builders ) {
			aggregatedElements.addAll( builder.build() );
		}

		return new BeanMetaDataImpl<>(
				constrainedType,
				defaultGroupSequence,
				defaultGroupSequenceProvider,
				aggregatedElements,
				validationOrderGenerator,
				beanMetadataHierarchy
		);
	}

	private static class BuilderDelegate {
		private final HibernateConstrainedType<?> constrainedType;
		private final ConstrainedElement constrainedElement;
		private final ConstraintCreationContext constraintCreationContext;
		private final ExecutableHelper executableHelper;
		private final ExecutableParameterNameProvider parameterNameProvider;
		private MetaDataBuilder metaDataBuilder;
		private ExecutableMetaData.Builder methodBuilder;
		private final MethodValidationConfiguration methodValidationConfiguration;
		private final int hashCode;

		public BuilderDelegate(
				HibernateConstrainedType<?> constrainedType,
				ConstrainedElement constrainedElement,
				ConstraintCreationContext constraintCreationContext,
				ExecutableHelper executableHelper,
				ExecutableParameterNameProvider parameterNameProvider,
				MethodValidationConfiguration methodValidationConfiguration
		) {
			this.constrainedType = constrainedType;
			this.constrainedElement = constrainedElement;
			this.constraintCreationContext = constraintCreationContext;
			this.executableHelper = executableHelper;
			this.parameterNameProvider = parameterNameProvider;
			this.methodValidationConfiguration = methodValidationConfiguration;

			switch ( constrainedElement.getKind() ) {
				case FIELD:
					ConstrainedField constrainedField = (ConstrainedField) constrainedElement;
					metaDataBuilder = new PropertyMetaData.Builder(
							constrainedType,
							constrainedField,
							constraintCreationContext
					);
					break;
				case CONSTRUCTOR:
				case METHOD:
				case GETTER:
					ConstrainedExecutable constrainedExecutable = (ConstrainedExecutable) constrainedElement;
					Callable callable = constrainedExecutable.getCallable();

					// HV-890 Not adding meta-data for private super-type methods to the method meta-data of this bean;
					// It is not needed and it may conflict with sub-type methods of the same signature
					if ( !callable.isPrivate() || constrainedType.getActuallClass() == callable.getDeclaringClass() ) {
						methodBuilder = new ExecutableMetaData.Builder(
								constrainedType,
								constrainedExecutable,
								constraintCreationContext,
								executableHelper,
								parameterNameProvider,
								methodValidationConfiguration
						);
					}

					if ( constrainedElement.getKind() == ConstrainedElement.ConstrainedElementKind.GETTER ) {
						metaDataBuilder = new PropertyMetaData.Builder(
								constrainedType,
								constrainedExecutable,
								constraintCreationContext
						);
					}
					break;
				case TYPE:
					ConstrainedType constrainedTypeElement = (ConstrainedType) constrainedElement;
					metaDataBuilder = new ClassMetaData.Builder(
							constrainedType,
							constrainedTypeElement,
							constraintCreationContext
					);
					break;
				default:
					throw new IllegalStateException(
							StringHelper.format( "Constrained element kind '%1$s' not supported here.", constrainedElement.getKind() ) );
			}

			this.hashCode = buildHashCode();
		}

		public boolean add(ConstrainedElement constrainedElement) {
			boolean added = false;

			if ( methodBuilder != null && methodBuilder.accepts( constrainedElement ) ) {
				methodBuilder.add( constrainedElement );
				added = true;
			}

			if ( metaDataBuilder != null && metaDataBuilder.accepts( constrainedElement ) ) {
				metaDataBuilder.add( constrainedElement );

				if ( !added && constrainedElement.getKind().isMethod() && methodBuilder == null ) {
					ConstrainedExecutable constrainedMethod = (ConstrainedExecutable) constrainedElement;
					methodBuilder = new ExecutableMetaData.Builder(
							constrainedType,
							constrainedMethod,
							constraintCreationContext,
							executableHelper,
							parameterNameProvider,
							methodValidationConfiguration
					);
				}

				added = true;
			}

			return added;
		}

		public Set<ConstraintMetaData> build() {
			Set<ConstraintMetaData> metaDataSet = newHashSet();

			if ( metaDataBuilder != null ) {
				metaDataSet.add( metaDataBuilder.build() );
			}

			if ( methodBuilder != null ) {
				metaDataSet.add( methodBuilder.build() );
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
			result = prime * result + constrainedType.hashCode();
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
			if ( !constrainedType.equals( other.constrainedType ) ) {
				return false;
			}
			if ( !constrainedElement.equals( other.constrainedElement ) ) {
				return false;
			}
			return true;
		}
	}
}
