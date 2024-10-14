/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations;

import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import jakarta.validation.Validator;

import org.testng.annotations.BeforeMethod;

/**
 * @author Marko Bekhta
 */
public abstract class AbstractConstrainedTest {

	protected Validator validator;

	@BeforeMethod
	public void setUp() throws Exception {
		validator = getValidator();
	}
}
