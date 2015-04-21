package org.hibernate.validator.referenceguide.chapter11.context;

import java.util.Date;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Future;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

public class MyFutureValidator implements ConstraintValidator<Future, Date> {

	@Override
	public void initialize(Future constraintAnnotation) {
	}

	@Override
	public boolean isValid(Date value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		HibernateConstraintValidatorContext hibernateContext = context.unwrap(
				HibernateConstraintValidatorContext.class
		);

		Date now = new Date( hibernateContext.getTimeProvider().getCurrentTime() );

		if ( !value.after( now ) ) {
			hibernateContext.disableDefaultConstraintViolation();
			hibernateContext.addExpressionVariable( "now", now )
					.buildConstraintViolationWithTemplate( "Must be after ${now}" )
					.addConstraintViolation();

			return false;
		}

		return true;
	}
}
