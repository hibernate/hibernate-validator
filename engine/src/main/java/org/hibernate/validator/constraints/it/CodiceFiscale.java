package org.hibernate.validator.constraints.it;

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

import org.hibernate.validator.Incubating;

/**
 * Validates that a {@code CharSequence} is an Italian tax code (codice fiscale).
 * <p>
 * Both forms are accepted: the 16-character code of natural persons, including the variants
 * in which digits are replaced by letters to tell apart two people who would otherwise share
 * the same code ("omocodia"), and the 11-digit code of legal entities.
 * <p>
 * Only the format and the check character are verified: whether a code has actually been issued,
 * and to whom, is known to the Italian Revenue Agency alone.
 *
 * @since 9.2
 */
@Incubating
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(CodiceFiscale.List.class)
@ReportAsSingleViolation
public @interface CodiceFiscale {

	String message() default "{org.hibernate.validator.constraints.it.CodiceFiscale.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * Defines several {@code @CodiceFiscale} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		CodiceFiscale[] value();
	}
}
