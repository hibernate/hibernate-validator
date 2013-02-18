/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.test.internal.engine.groups.conversion;

import javax.validation.Configuration;
import javax.validation.ValidatorFactory;

import org.testng.annotations.BeforeMethod;

import org.hibernate.validator.testutil.ValidatorUtil;

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
