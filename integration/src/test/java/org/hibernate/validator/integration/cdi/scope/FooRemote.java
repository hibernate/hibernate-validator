/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.scope;

import javax.ejb.Remote;

@Remote
public interface FooRemote {
	/**
	 * verify if the validator is indeed using the correct validation configuration.
	 *
	 * @param testValue an integer to test
	 *
	 * @return an integer that indicates the total number of the violations.
	 */
	int verifyValidatorOnPojo(int testValue);
}
