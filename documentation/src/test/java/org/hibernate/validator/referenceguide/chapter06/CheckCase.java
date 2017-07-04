//tag::include[]
package org.hibernate.validator.referenceguide.chapter06;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

//end::include[]

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import org.hibernate.validator.referenceguide.chapter06.CheckCase.List;

//tag::include[]
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = CheckCaseValidator.class)
@Documented
@Repeatable(List.class)
public @interface CheckCase {

	String message() default "{org.hibernate.validator.referenceguide.chapter06.CheckCase." +
			"message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	CaseMode value();

	@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	@interface List {
		CheckCase[] value();
	}
}
//end::include[]
