/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.constraints.br;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates a CPF (Cadastro de Pessoa F\u00edsica - Brazilian individual taxpayer registry number).
 *
 * @author George Gastaldi
 * @author Victor Rezende dos Santos
 */
@Pattern.List({
		@Pattern(regexp = "([0-9]{3}[.]?[0-9]{3}[.]?[0-9]{3}-[0-9]{2})|([0-9]{11})"),
		// XXX.XXX.XXX-XX where X is always the same digit are not a valid CPFs, but all of them passes the mod check. Needs to be singled out each one via regexp
		@Pattern(regexp = "^(?:(?!000\\.?000\\.?000-?00).)*$"),
		@Pattern(regexp = "^(?:(?!111\\.?111\\.?111-?11).)*$"),
		@Pattern(regexp = "^(?:(?!222\\.?222\\.?222-?22).)*$"),
		@Pattern(regexp = "^(?:(?!333\\.?333\\.?333-?33).)*$"),
		@Pattern(regexp = "^(?:(?!444\\.?444\\.?444-?44).)*$"),
		@Pattern(regexp = "^(?:(?!555\\.?555\\.?555-?55).)*$"),
		@Pattern(regexp = "^(?:(?!666\\.?666\\.?666-?66).)*$"),
		@Pattern(regexp = "^(?:(?!777\\.?777\\.?777-?77).)*$"),
		@Pattern(regexp = "^(?:(?!888\\.?888\\.?888-?88).)*$"),
		@Pattern(regexp = "^(?:(?!999\\.?999\\.?999-?99).)*$")
})
@ReportAsSingleViolation
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface CPF {
	String message() default "{org.hibernate.validator.constraints.br.CPF.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
