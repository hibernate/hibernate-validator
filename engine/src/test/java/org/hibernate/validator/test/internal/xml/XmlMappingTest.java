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
package org.hibernate.validator.test.internal.xml;

import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.BootstrapConfiguration;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableType;
import javax.validation.groups.Default;
import javax.validation.metadata.MethodDescriptor;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;
import org.testng.annotations.Test;

import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.testng.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class XmlMappingTest {

	@Test
	@TestForIssue(jiraKey = "HV-214")
	public void testConstraintInheritanceWithXmlConfiguration() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		final Set<ConstraintViolation<Customer>> violations = validator.validate( new Customer(), Default.class );

		assertEquals( violations.size(), 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-252")
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
	@TestForIssue(jiraKey = "HV-262")
	public void testInterfaceConfiguration() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "my-interface-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertEquals( violations.size(), 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-262")
	public void testInterfaceImplementationConfiguration() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "my-interface-impl-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertEquals( violations.size(), 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-263")
	public void testEmptyInterfaceConfiguration() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "empty-my-interface-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertEquals( violations.size(), 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-480")
	public void testConstraintsFromXmlAndProgrammaticApiAddUp() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );

		//given
		final ConstraintMapping programmaticMapping = configuration.createConstraintMapping();
		programmaticMapping.type( Customer.class )
				.property( "firstName", ElementType.FIELD )
				.constraint( new SizeDef().min( 2 ).max( 10 ) );

		final InputStream xmlMapping = XmlMappingTest.class.getResourceAsStream( "hv-480-mapping.xml" );

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

	@Test
	public void shouldLoadBv11ConstraintMapping() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "my-interface-impl-mapping-bv-1.1.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertEquals( violations.size(), 1 );
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV000122: Unsupported schema version for constraint mapping file: 2\\.0\\."
	)
	public void shouldFailToLoadConstraintMappingWithUnsupportedVersion() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping(
				XmlMappingTest.class.getResourceAsStream(
						"my-interface-impl-mapping-unsupported-version.xml"
				)
		);

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertEquals( violations.size(), 1 );
	}

	@Test
	public void testParameterNameProviderConfiguration() {
		runWithCustomValidationXml(
				"parameter-name-provider-validation.xml", new Runnable() {

			@Override
			public void run() {
				//given
				Validator validator = ValidatorUtil.getValidator();
				BootstrapConfiguration bootstrapConfiguration = ValidatorUtil.getConfiguration()
						.getBootstrapConfiguration();

				//when
				MethodDescriptor methodDescriptor = validator.getConstraintsForClass( CustomerService.class )
						.getConstraintsForMethod( "createCustomer", Customer.class );

				//then
				assertEquals(
						bootstrapConfiguration.getParameterNameProviderClassName(),
						CustomParameterNameProvider.class.getName()
				);

				assertEquals( methodDescriptor.getParameterDescriptors().get( 0 ).getName(), "param0" );
			}
		}
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-707")
	public void shouldReturnDefaultExecutableTypesForValidationXmlWithoutTypesGiven() {
		runWithCustomValidationXml(
				"bv-1.0-validation.xml", new Runnable() {

			@Override
			public void run() {
				//given
				BootstrapConfiguration bootstrapConfiguration = ValidatorUtil.getConfiguration()
						.getBootstrapConfiguration();

				//when
				//then
				assertEquals(
						bootstrapConfiguration.getDefaultValidatedExecutableTypes(),
						asSet( ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS )
				);
			}
		}
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-707")
	public void shouldReturnDefaultExecutableTypesIfNoValidationXmlIsGiven() {
		//given
		BootstrapConfiguration bootstrapConfiguration = ValidatorUtil.getConfiguration()
				.getBootstrapConfiguration();

		//when
		//then
		assertEquals(
				bootstrapConfiguration.getDefaultValidatedExecutableTypes(),
				asSet( ExecutableType.CONSTRUCTORS, ExecutableType.NON_GETTER_METHODS )
		);
	}

	@Test
	public void testLoadingOfBv10ValidationXml() {
		runWithCustomValidationXml(
				"bv-1.0-validation.xml", new Runnable() {

			@Override
			public void run() {
				//given
				BootstrapConfiguration bootstrapConfiguration = ValidatorUtil.getConfiguration()
						.getBootstrapConfiguration();

				//when
				//then
				assertEquals(
						bootstrapConfiguration.getProperties().get( "com.acme.validation.safetyChecking" ),
						"failOnError"
				);
			}
		}
		);
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV000122: Unsupported schema version for META-INF/validation.xml: 2\\.0\\."
	)
	public void shouldFailToLoadValidationXmlWithUnsupportedVersion() {
		runWithCustomValidationXml(
				"unsupported-validation.xml", new Runnable() {

			@Override
			public void run() {
				ValidatorUtil.getConfiguration().getBootstrapConfiguration();
			}
		}
		);
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV000100: Unable to parse META-INF/validation.xml."
	)
	public void shouldFailToLoad10ValidationXmlWithParameterNameProvider() {
		runWithCustomValidationXml(
				"invalid-bv-1.0-validation.xml", new Runnable() {

			@Override
			public void run() {
				ValidatorUtil.getConfiguration().getBootstrapConfiguration();
			}
		}
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-836")
	public void testCascadedValidation() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "cascaded-validation-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		System system = new System();
		system.addPart( new Part() );
		Set<ConstraintViolation<System>> violations = validator.validate( system );

		assertEquals( violations.size(), 1 );
		assertCorrectConstraintTypes( violations, NotNull.class );
	}

	/**
	 * Executes the given runnable, using the specified file as replacement for
	 * {@code META-INF/validation.xml}.
	 *
	 * @param validationXmlName The file to be used as validation.xml file.
	 * @param runnable The runnable to execute.
	 */
	private void runWithCustomValidationXml(final String validationXmlName, Runnable runnable) {
		ClassLoader previousContextCl = Thread.currentThread().getContextClassLoader();

		try {
			Thread.currentThread().setContextClassLoader(
					new ClassLoader( previousContextCl ) {
						@Override
						public InputStream getResourceAsStream(String name) {
							if ( name.equals( "META-INF/validation.xml" ) ) {
								return XmlMappingTest.class.getResourceAsStream( validationXmlName );
							}

							return super.getResourceAsStream( name );
						}
					}
			);
			runnable.run();
		}
		finally {
			Thread.currentThread().setContextClassLoader( previousContextCl );
		}
	}
}
