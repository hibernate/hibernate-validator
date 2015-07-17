/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata;

import javax.validation.constraints.Min;

/**
 * @author Gunnar Morling
 */
public class IllegalCustomerRepositoryExt extends CustomerRepository {

	@Override
	public void zap(@Min(0) int i) {
	}
}
