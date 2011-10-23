/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.xml;

import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.groups.Default;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.testng.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class XmlMappingTest {

	@Test
	/**
	 * HV-214
	 */
	public void testConstraintInheritanceWithXmlConfiguration() {

		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		final Set<ConstraintViolation<Customer>> violations = validator.validate( new Customer(), Default.class );

		assertEquals( violations.size(), 1 );
	}

	@Test
	/**
	 * HV-252
	 */
	public void testListOfString() {

		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "properties-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		List<String> listOfString = new ArrayList<String>();
		listOfString.add( "one" );
		listOfString.add( "two" );
		listOfString.add( "three" );

		final Set<ConstraintViolation<Properties>> violations = validator.validateValue(
				Properties.class, "listOfString", listOfString
		);

		assertEquals( violations.size(), 0 );
	}

	@Test
	/**
	 * HV-262
	 */
	public void testInterfaceConfiguration() {

		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "my-interface-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertEquals( violations.size(), 1 );
	}

	@Test
	/**
	 * HV-262
	 */
	public void testInterfaceImplementationConfiguration() {

		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "my-interface-impl-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertEquals( violations.size(), 1 );
	}

	@Test
	/**
	 * HV-263
	 */
	public void testEmptyInterfaceConfiguration() {

		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "empty-my-interface-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertEquals( violations.size(), 0 );
	}

	/**
	 * HV-480
	 */
	@Test
	public void testConstraintsFromXmlAndProgrammaticApiAddUp() {

		//given
		final ConstraintMapping programmaticMapping = new ConstraintMapping();
		programmaticMapping.type( Customer.class )
				.property( "firstName", ElementType.FIELD )
				.constraint( new SizeDef().min( 2 ).max( 10 ) );

		final InputStream xmlMapping = XmlMappingTest.class.getResourceAsStream( "hv-480-mapping.xml" );

		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );

		configuration.addMapping( programmaticMapping );
		configuration.addMapping( xmlMapping );

		final Customer customer = new Customer();
		customer.setFirstName( "" );

		//when
		final Set<ConstraintViolation<Customer>> violations = configuration.buildValidatorFactory()
				.getValidator()
				.validate(
						customer
				);

		//then
		assertCorrectConstraintViolationMessages(
				violations,
				"size must be between 1 and 10",
				"size must be between 2 and 10"
		);
	}
}
