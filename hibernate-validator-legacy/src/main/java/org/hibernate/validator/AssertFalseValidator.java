package org.hibernate.validator;

import java.io.Serializable;


/**
 * Check if a given object is false or not
 *
 * @author Gavin King
 * @author Hardy Ferentschik
 */
public class AssertFalseValidator implements Validator<AssertFalse>, Serializable {

    public boolean isValid(Object value) {
        if (value == null) return true;
        if (value instanceof Boolean) {
            return !(Boolean) value;
        }
        return false;
    }

    public void initialize(AssertFalse parameters) {
    }

}
