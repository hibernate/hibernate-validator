package org.hibernate.validator.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.ModCheck.List;
import org.hibernate.validator.constraints.ModCheck.ModType;

/**
 * Validates a CNPJ (Cadastro de Pessoa Jurídica)
 * 
 * @author George Gastaldi
 * 
 */
@Pattern(regexp = "([0-9]{2}[.]?[0-9]{3}[.]?[0-9]{3}[/]?[0-9]{4}[-]?[0-9]{2})")
@List({ 
	@ModCheck(value = ModType.MOD11, checkDigitIndex = 12, multiplier = 9, rangeEnd = 12),
	@ModCheck(value = ModType.MOD11, checkDigitIndex = 13, multiplier = 9, rangeEnd = 13) 
})
@ReportAsSingleViolation
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface CNPJ {

	String message() default "{org.hibernate.validator.constraints.CNPJ.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

}
