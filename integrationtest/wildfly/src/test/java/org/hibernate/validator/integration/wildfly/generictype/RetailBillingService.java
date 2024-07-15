/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.generictype;

import jakarta.validation.constraints.Min;

public class RetailBillingService implements BillingService<String> {

	@Override
	@Min(0)
	public int getBillingAmount(String order) {
		return -1;
	}
}
