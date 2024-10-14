/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
