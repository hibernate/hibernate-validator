/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.util.List;
import java.util.Set;

import javax.validation.ElementKind;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ClassDescriptorImpl;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

/**
 * Represents the constraint related meta data for a type i.e. class-level
 * constraints.
 *
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @author Marko Bekhta
 */
public class ClassMetaData extends AbstractConstraintMetaData {

	private ClassMetaData(Class<?> beanClass,
			Set<MetaConstraint<?>> constraints,
			Set<MetaConstraint<?>> containerElementsConstraints) {
		super(
				beanClass.getSimpleName(),
				beanClass,
				constraints,
				containerElementsConstraints,
				false,
				!constraints.isEmpty() || !containerElementsConstraints.isEmpty()
		);
	}

	@Override
	public ClassDescriptorImpl asDescriptor(boolean defaultGroupSequenceRedefined, List<Class<?>> defaultGroupSequence) {
		return new ClassDescriptorImpl(
				getType(),
				asDescriptors( getDirectConstraints() ),
				defaultGroupSequenceRedefined,
				defaultGroupSequence
		);
	}

	@Override
	public String toString() {
		return "ClassLevelMetaData [type=" + getType() + "]]";
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.BEAN;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
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
		return true;
	}

	public static class Builder extends MetaDataBuilder {
		public Builder(Class<?> beanClass, ConstrainedType constrainedType, ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
				ValueExtractorManager valueExtractorManager) {
			super( beanClass, constraintHelper, typeResolutionHelper, valueExtractorManager );

			add( constrainedType );
		}

		@Override
		public boolean accepts(ConstrainedElement constrainedElement) {
			return constrainedElement.getKind() == ConstrainedElement.ConstrainedElementKind.TYPE;
		}

		@Override
		public final void add(ConstrainedElement constrainedElement) {
			super.add( constrainedElement );
		}

		@Override
		public ClassMetaData build() {
			return new ClassMetaData(
					getBeanClass(),
					adaptOriginsAndImplicitGroups( getDirectConstraints() ),
					adaptOriginsAndImplicitGroups( getContainerElementConstraints() )
			);
		}
	}
}
