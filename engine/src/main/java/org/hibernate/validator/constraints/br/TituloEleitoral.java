/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.constraints.br;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraintvalidation.SupportedValidationTarget;
import jakarta.validation.constraintvalidation.ValidationTarget;

import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.br.TituloEleitoral.List;

/**
 * Validates a <a href="https://pt.wikipedia.org/wiki/T%C3%ADtulo_de_eleitor">T\u00edtulo Eleitoral</a> (Brazilian Voter ID card number).
 *
 * @author George Gastaldi
 */
@Pattern(regexp = "[0-9]{12}")
@Mod11Check.List({
		@Mod11Check(threshold = 9,
				endIndex = 7,
				checkDigitIndex = 10,
				treatCheck10As = '0'),
		@Mod11Check(threshold = 9,
				startIndex = 8,
				endIndex = 10,
				checkDigitIndex = 11,
				treatCheck10As = '0')
})
@ReportAsSingleViolation
@Documented
@Constraint(validatedBy = { })
@SupportedValidationTarget(ValidationTarget.ANNOTATED_ELEMENT)
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface TituloEleitoral {
	String message() default "{org.hibernate.validator.constraints.br.TituloEleitoral.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Defines several {@code @TituloEleitoral} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		TituloEleitoral[] value();
	}
}
