package org.hibernate.validator.constraints.impl;

import org.joda.time.base.AbstractInstant;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Future;
import java.lang.annotation.Annotation;

/**
 * Check if Joda Time type who inherit from
 * {@code org.joda.time.base.AbstractInstant}
 * is in the future.
 *
 * @author Kevin Pollet
 */
public class FutureValidatorForAbstractInstant implements ConstraintValidator<Future, AbstractInstant> {


    public void initialize(Future constraintAnnotation) {
    }

    public boolean isValid(AbstractInstant value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return value.isAfterNow();
    }
}
