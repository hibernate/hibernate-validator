package org.hibernate.validator.referenceguide.chapter11.booleancomposition;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.ConstraintComposition;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import static org.hibernate.validator.constraints.CompositionType.OR;

@ConstraintComposition(OR)
@Pattern(regexp = "[a-z]")
@Size(min = 2, max = 3)
@ReportAsSingleViolation
@Target({ METHOD, FIELD })
@Retention(RUNTIME)
@Constraint(validatedBy = { })
public @interface PatternOrSize {
	String message() default "{org.hibernate.validator.referenceguide.chapter11." +
			"booleancomposition.PatternOrSize.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
