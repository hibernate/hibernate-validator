/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.xml;

import jakarta.validation.Configuration;
import jakarta.validation.ValidatorFactory;

import org.testng.annotations.BeforeMethod;

import org.hibernate.validator.test.internal.engine.methodvalidation.AbstractConstructorValidationTest;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

/**
 * Tests for XML configured constructor validation.
 *
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = "HV-373")
public class XmlBasedConstructorValidationTest extends AbstractConstructorValidationTest {
	@Override
	@BeforeMethod
	public void setUp() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				XmlBasedConstructorValidationTest.class.getResourceAsStream(
						"constructor-validation-mapping.xml"
				)
		);

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		this.executableValidator = validatorFactory.getValidator().forExecutables();
	}

	@Override
	public String messagePrefix() {
		return "[XML] - ";
	}
}
