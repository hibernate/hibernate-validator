//$Id$
package org.hibernate.validator.test;

import java.io.Serializable;

import org.hibernate.validator.Validator;

/**
 * Sample of a bean-level validator
 *
 * @author Emmanuel Bernard
 */
public class SerializabilityValidator implements Validator<Serializability>, Serializable {
	public boolean isValid(Object value) {
		return value instanceof Serializable;
	}

	public void initialize(Serializability parameters) {

	}
}
