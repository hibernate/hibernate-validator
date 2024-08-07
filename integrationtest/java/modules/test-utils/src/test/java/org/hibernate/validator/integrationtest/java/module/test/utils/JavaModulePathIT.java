/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integrationtest.java.module.test.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class JavaModulePathIT {

	@Test
	public void test() {
		assertThat( Car.test() ).isTrue();
	}

}
