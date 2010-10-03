package org.hibernate.validator.constraints.impl;

import org.joda.time.base.AbstractInstant;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Future;
import javax.validation.constraints.Past;

/**
 * Check if Joda Time type who inherit from
 * {@code org.joda.time.base.AbstractInstant}
 * is in the past.
 *
 * @author Kevin Pollet
 */
public class PastValidatorForAbstractInstant implements ConstraintValidator<Past, AbstractInstant> {


    public void initialize(Past constraintAnnotation) {
    }

    public boolean isValid(AbstractInstant value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        
        return value.isBeforeNow();
    }
}
