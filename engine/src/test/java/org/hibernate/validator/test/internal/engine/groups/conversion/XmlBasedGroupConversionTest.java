/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.groups.conversion;

import jakarta.validation.Configuration;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.BeforeEach;

/**
 * Integrative test for XML configured group conversion.
 *
 * @author Hardy Ferentschik
 */
public class XmlBasedGroupConversionTest extends AbstractGroupConversionTest {
	@Override
	@BeforeEach
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


	@Override
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
