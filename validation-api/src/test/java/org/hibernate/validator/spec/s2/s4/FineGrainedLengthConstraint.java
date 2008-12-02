package org.hibernate.validator.spec.s2.s4;

import javax.validation.ConstraintContext;
import javax.validation.Constraint;

/**
 * Check that a string length is between min and max
 * Error messages are using either key:
 *  - error.length.min if the min limit is reached
 *  - error.length.max if the max limit is reached
 */
public class FineGrainedLengthConstraint implements Constraint<Length> {
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
    public boolean isValid(Object value, ConstraintContext constraintContext) {
        if ( value == null ) return true;
        if ( !( value instanceof String ) ) {
            throw new IllegalArgumentException("Expected String type");
        }
        String string = (String) value;
        int length = string.length();

		//remove default error
		constraintContext.disableDefaultError();

		if (length < min) {
			//add min specific error
			constraintContext.addError( "{error.length.min}" );
			return false;
		}
		if (length > max) {
			//add max specific error
			constraintContext.addError( "{error.length.max}" );
			return false;
		}
		return true;
    }
}