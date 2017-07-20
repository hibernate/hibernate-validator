/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.lang.annotation.ElementType;
import java.lang.reflect.Executable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.validation.ElementKind;
import javax.validation.metadata.ParameterDescriptor;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ParameterDescriptorImpl;
import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * An aggregated view of the constraint related meta data for a single method
 * parameter.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class ParameterMetaData extends AbstractConstraintMetaData implements Cascadable {

	private final int index;
	private final CascadingMetaData cascadingMetaData;

	private ParameterMetaData(int index,
							  String name,
							  Type type,
							  Set<MetaConstraint<?>> constraints,
							  Set<MetaConstraint<?>> containerElementsConstraints,
							  CascadingMetaData cascadingMetaData) {
		super(
				name,
				type,
				constraints,
				containerElementsConstraints,
				cascadingMetaData.isMarkedForCascadingOnAnnotatedObjectOrContainerElements(),
				!constraints.isEmpty() || !containerElementsConstraints.isEmpty() || cascadingMetaData.isMarkedForCascadingOnAnnotatedObjectOrContainerElements()
		);

		this.index = index;

		this.cascadingMetaData = cascadingMetaData;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public ElementType getElementType() {
		return ElementType.PARAMETER;
	}

	@Override
	public ParameterDescriptor asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new ParameterDescriptorImpl(
				getType(),
				index,
				getName(),
				asDescriptors( getDirectConstraints() ),
				asContainerElementTypeDescriptors( getContainerElementsConstraints(), cascadingMetaData, defaultGroupSequenceRedefined, defaultGroupSequence ),
				cascadingMetaData.isCascading(),
				defaultGroupSequenceRedefined,
				defaultGroupSequence,
				cascadingMetaData.getGroupConversionDescriptors()
		);
	}

	@Override
	public Object getValue(Object parent) {
		return ( (Object[]) parent )[getIndex()];
	}

	@Override
	public Type getCascadableType() {
		return getType();
	}

	@Override
	public void appendTo(PathImpl path) {
		path.addParameterNode( getName(), getIndex() );
	}

	@Override
	public CascadingMetaData getCascadingMetaData() {
		return cascadingMetaData;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.PARAMETER;
	}

	public static class Builder extends MetaDataBuilder {
		private final ExecutableParameterNameProvider parameterNameProvider;
		private final Type parameterType;
		private final int parameterIndex;
		private Executable executableForNameRetrieval;
		private CascadingMetaDataBuilder cascadingMetaDataBuilder;

		public Builder(Class<?> beanClass,
				ConstrainedParameter constrainedParameter,
				ConstraintHelper constraintHelper,
				TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager,
				ExecutableParameterNameProvider parameterNameProvider) {
			super( beanClass, constraintHelper, typeResolutionHelper, valueExtractorManager );

			this.parameterNameProvider = parameterNameProvider;
			this.parameterType = constrainedParameter.getType();
			this.parameterIndex = constrainedParameter.getIndex();

			add( constrainedParameter );
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {
			if ( constrainedElement.getKind() != ConstrainedElementKind.PARAMETER ) {
				return false;
			}

			return ( (ConstrainedParameter) constrainedElement ).getIndex() == parameterIndex;
		}

		@Override
		public void add(ConstrainedElement constrainedElement) {
			super.add( constrainedElement );

			ConstrainedParameter newConstrainedParameter = (ConstrainedParameter) constrainedElement;

			if ( cascadingMetaDataBuilder == null ) {
				cascadingMetaDataBuilder = newConstrainedParameter.getCascadingMetaDataBuilder();
			}
			else {
				cascadingMetaDataBuilder = cascadingMetaDataBuilder.merge( newConstrainedParameter.getCascadingMetaDataBuilder() );
			}

			// If the current parameter is from a method hosted on a parent class,
			// use this parent class parameter name instead of the more specific one.
			// Worse case, we are consistent, best case parameters from parents are more meaningful.
			// See HV-887 and the associated unit test
			if ( executableForNameRetrieval == null ||
					newConstrainedParameter.getExecutable().getDeclaringClass().isAssignableFrom( executableForNameRetrieval.getDeclaringClass() ) ) {
				executableForNameRetrieval = newConstrainedParameter.getExecutable();
			}
		}

		@Override
		public ParameterMetaData build() {
			return new ParameterMetaData(
					parameterIndex,
					parameterNameProvider.getParameterNames( executableForNameRetrieval ).get( parameterIndex ),
					parameterType,
					adaptOriginsAndImplicitGroups( getDirectConstraints() ),
					adaptOriginsAndImplicitGroups( getContainerElementConstraints() ),
					cascadingMetaDataBuilder.build( valueExtractorManager, executableForNameRetrieval )
			);
		}
	}
}
