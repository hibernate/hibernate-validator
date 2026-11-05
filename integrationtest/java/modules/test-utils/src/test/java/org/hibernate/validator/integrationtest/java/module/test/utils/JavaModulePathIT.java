package org.hibernate.validator.integrationtest.java.module.test.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class JavaModulePathIT {

	@Test
	public void test() {
		assertThat( Car.test() ).isTrue();
	}

}
