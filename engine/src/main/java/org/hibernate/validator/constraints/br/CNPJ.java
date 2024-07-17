/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraints.br.CNPJ.List;

/**
 * Validates a CNPJ (Cadastro de Pessoa Jur\u00eddica - Brazilian corporate tax payer registry number).
 *
 * @author George Gastaldi
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface CNPJ {
	String message() default "{org.hibernate.validator.constraints.br.CNPJ.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return The format type of the CNPJ number that should be considered as valid.
	 * @see Format
	 */
	@Incubating
	Format format() default Format.NUMERIC;

	enum Format {
		/**
		 * The older, original, CNPJ format that is constructed from digits only,
		 * e.g. {@code dd.ddd.ddd/dddd-dd}, where {@code d} represents a digit.
		 */
		NUMERIC,
		/**
		 * The new CNPJ format that is constructed from digits and ASCII letters,
		 * e.g. {@code ss.sss.sss/ssss-dd}, where {@code d} represents a digit and {@code s} represents either a digit or a letter.
		 * This format adoption should start in January 2026.
		 */
		ALPHANUMERIC;
	}

	/**
	 * Defines several {@code @CNPJ} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		CNPJ[] value();
	}
}
