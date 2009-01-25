package org.hibernate.validator.spec.s2.s4;

import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintValidator;

/**
 * Check that a string length is between min and max
 *
 */
public class LengthConstraintValidator implements ConstraintValidator<Length> {
    private int min;
    private int max;

    /**
     * Configure the constraint validator based on the elements
     * specified at the time it was defined.
     * @param constraint the constraint definition
     */
    public void initialize(Length constraint) {
        min = constraint.min();
        max = constraint.max();
    }

    /**
     * Validate a specified value.
     * returns false if the specified value does not conform to the definition
     * @exception IllegalArgumentException if the object is not of type String
     */
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if ( value == null ) return true;
        if ( !( value instanceof String ) ) {
            throw new IllegalArgumentException("Expected String type");
        }
        String string = (String) value;
        int length = string.length();
        return length >= min && length <= max;
    }
}
