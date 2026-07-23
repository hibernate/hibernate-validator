/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import org.junit.jupiter.api.BeforeEach;

/**
 * Tests for annotation configured constructor validation.
 *
 * @author Hardy Ferentschik
 */
public class AnnotationBasedConstructorValidationTest extends AbstractConstructorValidationTest {
	@Override
	@BeforeEach
	public void setUp() {
		this.executableValidator = getValidator().forExecutables();
	}

	@Override
	public String messagePrefix() {
		return "";
	}
}
