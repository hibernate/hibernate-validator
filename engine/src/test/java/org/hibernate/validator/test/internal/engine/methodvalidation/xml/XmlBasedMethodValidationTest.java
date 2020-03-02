/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.xml;

import jakarta.validation.Configuration;

import org.testng.annotations.BeforeMethod;

import org.hibernate.validator.test.internal.engine.methodvalidation.AbstractMethodValidationTest;
import org.hibernate.validator.testutils.ValidatorUtil;

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
