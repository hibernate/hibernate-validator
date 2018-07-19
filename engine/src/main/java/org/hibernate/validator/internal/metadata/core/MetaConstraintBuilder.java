/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.core;

import java.lang.annotation.Annotation;

import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

/**
 * @author Marko Bekhta
 */
public class MetaConstraintBuilder<A extends Annotation> {

	private final ConstraintAnnotationDescriptor<A> annotationDescriptor;
	private final ConstraintLocation.Builder locationBuilder;

	public MetaConstraintBuilder(ConstraintAnnotationDescriptor<A> annotationDescriptor, ConstraintLocation.Builder locationBuilder) {
		this.annotationDescriptor = annotationDescriptor;
		this.locationBuilder = locationBuilder;
	}

	public MetaConstraint<A> build(TypeResolutionHelper typeResolutionHelper, ConstraintHelper constraintHelper, ValueExtractorManager valueExtractorManager, Constrainable constrainable) {
		ConstraintLocation constraintLocation = locationBuilder.build( constrainable );
		return MetaConstraints.create(
				typeResolutionHelper,
				valueExtractorManager,
				new ConstraintDescriptorImpl<>(
						constraintHelper,
						constrainable,
						annotationDescriptor,
						constraintLocation.getKind()
				),
				constraintLocation );
	}
}
