//$Id$
package org.hibernate.validator;

import java.io.Serializable;


/**
 * Check whether an element is true or not
 *
 * @author Gavin King
 */
public class AssertTrueValidator implements Validator<AssertTrue>, Serializable {

	public boolean isValid(Object value) {
		return (Boolean) value;
	}

	public void initialize(AssertTrue parameters) {
	}

}
