package org.hibernate.validator.referenceguide.chapter12.failfastonpropertyviolation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

import org.hibernate.validator.referenceguide.chapter12.failfastonpropertyviolation.NonSelfPublishing.NonSelfPublishingValidator;

@Documented
@Constraint(validatedBy = { NonSelfPublishingValidator.class })
@Target({ TYPE })
@Retention(RUNTIME)
public @interface NonSelfPublishing {

	String message() default "{org.hibernate.validator.referenceguide.chapter12.failfastonpropertyviolation.NonSelfPublishing.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
	// tag::include[]
	class NonSelfPublishingValidator implements ConstraintValidator<NonSelfPublishing, Book> {

		@Override
		public boolean isValid(Book book, ConstraintValidatorContext context) {
			return !book.getAuthor().equals( book.getPublisher() );
		}
	}
	//end::include[]
}

