/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element has to represent a valid
 * credit card number. This is the Luhn algorithm implementation
 * which aims to check for user mistake, not credit card validity!
 *
 * @author Hardy Ferentschik
 * @author Emmanuel Bernard
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@ReportAsSingleViolation
@LuhnCheck
public @interface CreditCardNumber {
	String message() default "{org.hibernate.validator.constraints.CreditCardNumber.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return Whether non-digit characters in the validated input should be ignored ({@code true}) or result in a
	 * validation error ({@code false}). Default is {@code false}
	 */
	@OverridesAttribute(constraint = LuhnCheck.class, name = "ignoreNonDigitCharacters")
	boolean ignoreNonDigitCharacters() default false;

	/**
	 * Defines several {@code @CreditCardNumber} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		CreditCardNumber[] value();
	}
}
