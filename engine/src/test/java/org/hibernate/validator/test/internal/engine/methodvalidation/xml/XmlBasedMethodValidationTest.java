/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.xml;

import jakarta.validation.Configuration;

import org.hibernate.validator.test.internal.engine.methodvalidation.AbstractMethodValidationTest;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;

/**
 * @author Hardy Ferentschik
 */
public class XmlBasedMethodValidationTest extends AbstractMethodValidationTest {

	@Override
	@BeforeMethod
	protected void setUp() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				XmlBasedMethodValidationTest.class.getResourceAsStream(
						"method-validation-mapping.xml"
				)
		);
		validator = configuration.buildValidatorFactory().getValidator();
		createProxy();
	}

	@Override
	protected String messagePrefix() {
		return "[XML] - ";
	}
}
