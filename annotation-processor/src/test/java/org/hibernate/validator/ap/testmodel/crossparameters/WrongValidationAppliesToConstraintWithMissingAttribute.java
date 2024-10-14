/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.crossparameters;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Two different validators are specified, so a 'validationAppliesTo' method is required.
 */
@Target({ ANNOTATION_TYPE, METHOD, CONSTRUCTOR })
@Retention(RUNTIME)
@Constraint(validatedBy = { GenericCrossParameterValidator.class, GenericNormalValidator.class })
@Documented
public @interface WrongValidationAppliesToConstraintWithMissingAttribute {

	String message() default "{WrongValidationAppliesToConstraintWithMissingAttribute.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

}
