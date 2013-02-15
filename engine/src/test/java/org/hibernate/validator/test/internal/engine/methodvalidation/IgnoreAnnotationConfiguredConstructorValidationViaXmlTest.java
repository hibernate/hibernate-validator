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
package org.hibernate.validator.test.internal.engine.methodvalidation;

import javax.validation.Configuration;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.metadata.BeanDescriptor;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Address;
import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepository;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.RepositoryBase;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.testng.Assert.assertFalse;

/**
 * @author Hardy Ferentschik
 */
public class IgnoreAnnotationConfiguredConstructorValidationViaXmlTest {
	private Validator validator;

	@BeforeTest
	public void setUp() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				IgnoreAnnotationConfiguredConstructorValidationViaXmlTest.class.getResourceAsStream(
						"constructor-validation-ignore-annotations.xml"
				)
		);

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();

		this.validator = validatorFactory.getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-373")
	public void testAllClassesUnConstrained() {
		Class<?>[] involvedClasses = new Class<?>[] {
				CustomerRepository.class,
				CustomerRepositoryImpl.class,
				RepositoryBase.class,
				Address.class,
				Customer.class
		};
		for ( Class<?> clazz : involvedClasses ) {
			BeanDescriptor beanDescriptor = validator.getConstraintsForClass( clazz );
			assertFalse(
					beanDescriptor.isBeanConstrained(),
					"All classes should be unconstrained due to xml configuration but " + clazz.getName() + "is not."
			);
		}
	}
}
