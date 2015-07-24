/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.groups.conversion;

import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.BeforeMethod;

import javax.validation.Configuration;
import javax.validation.ValidatorFactory;

/**
 * Integrative test for XML configured group conversion.
 *
 * @author Hardy Ferentschik
 */
public class XmlBasedGroupConversionTest extends AbstractGroupConversionTest {
	@BeforeMethod
	public void setupValidator() {
		Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				XmlBasedGroupConversionTest.class.getResourceAsStream(
						"group-conversion-mapping.xml"
				)
		);
		ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		validator = validatorFactory.getValidator();
	}


	public void conversionFromSequenceCausesException() {
		Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				XmlBasedGroupConversionTest.class.getResourceAsStream(
						"invalid-group-conversion-mapping.xml"
				)
		);
		ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		validator = validatorFactory.getValidator();

		super.conversionFromSequenceCausesException();
	}
}
