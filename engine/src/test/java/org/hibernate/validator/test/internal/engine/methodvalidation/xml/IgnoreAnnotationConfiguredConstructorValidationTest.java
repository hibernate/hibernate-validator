/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.methodvalidation.xml;

import static org.testng.Assert.assertFalse;

import jakarta.validation.Configuration;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;

import org.hibernate.validator.test.internal.engine.methodvalidation.model.Address;
import org.hibernate.validator.test.internal.engine.methodvalidation.model.Customer;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepository;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.CustomerRepositoryImpl;
import org.hibernate.validator.test.internal.engine.methodvalidation.service.RepositoryBase;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class IgnoreAnnotationConfiguredConstructorValidationTest {
	private Validator validator;

	@BeforeTest
	public void setUp() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				IgnoreAnnotationConfiguredConstructorValidationTest.class.getResourceAsStream(
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
