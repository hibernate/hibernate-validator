/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.serialization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;

/**
 * Denotes that a field should contain a valid email address.
 * <p/>
 * <p/>
 * Taken from <a href="http://blog.jteam.nl/2009/08/04/bean-validation-integrating-jsr-303-with-spring/"
 * >this blog</a>.
 *
 * @author Hardy Ferentschik
 */
@Documented
@Constraint(validatedBy = { DummyEmailValidator.class })
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Pattern(regexp = ".+@.+\\.[a-z]+")
@ReportAsSingleViolation
public @interface Email {

	String message() default "{org.hibernate.validator.internal.engine.serialization.Email.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
