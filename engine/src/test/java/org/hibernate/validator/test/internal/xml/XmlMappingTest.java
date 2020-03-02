/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertEquals;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.validation.BootstrapConfiguration;
import jakarta.validation.Configuration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.groups.Default;
import jakarta.validation.metadata.MethodDescriptor;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.SizeDef;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidationXmlTestHelper;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class XmlMappingTest {

	private static ValidationXmlTestHelper validationXmlTestHelper;

	@BeforeClass
	public static void setupValidationXmlTestHelper() {
		validationXmlTestHelper = new ValidationXmlTestHelper( XmlMappingTest.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-214")
	public void testConstraintInheritanceWithXmlConfiguration() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		final Set<ConstraintViolation<Customer>> violations = validator.validate( new Customer(), Default.class );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
		);
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

		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-262")
	public void testInterfaceConfiguration() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "my-interface-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-262")
	public void testInterfaceImplementationConfiguration() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "my-interface-impl-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-263")
	public void testEmptyInterfaceConfiguration() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "empty-my-interface-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertNoViolations( violations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-480")
	public void testConstraintsFromXmlAndProgrammaticApiAddUp() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );

		//given
		final ConstraintMapping programmaticMapping = configuration.createConstraintMapping();
		programmaticMapping.type( Customer.class )
				.field( "firstName" )
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
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size must be between 1 and 10" ),
				violationOf( Size.class ).withMessage( "size must be between 2 and 10" )
		);
	}

	@Test
	public void shouldLoadBv11ConstraintMapping() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "my-interface-impl-mapping-bv-1.1.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();
		final Set<ConstraintViolation<MyInterfaceImpl>> violations = validator.validate( new MyInterfaceImpl() );

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
		);
	}

	@Test(
			expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV000122: Unsupported schema version for constraint mapping file: 1\\.2\\."
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

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
		);
	}

	@Test
	public void testParameterNameProviderConfiguration() {
		validationXmlTestHelper.runWithCustomValidationXml(
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
	@TestForIssue(jiraKey = "HV-1463")
	public void testScriptEvaluatorFactoryConfiguration() {
		validationXmlTestHelper.runWithCustomValidationXml(
				"script-evaluator-factory-validation.xml", () -> {
					//given
					BootstrapConfiguration bootstrapConfiguration = ValidatorUtil.getConfiguration()
							.getBootstrapConfiguration();

					//then
					assertEquals(
							bootstrapConfiguration.getProperties().get( HibernateValidatorConfiguration.SCRIPT_EVALUATOR_FACTORY_CLASSNAME ),
							CustomScriptEvaluatorFactory.class.getName()
					);

				}
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1493")
	public void testTemporalValidationToleranceConfiguration() {
		validationXmlTestHelper.runWithCustomValidationXml(
				"temporal-validation-tolerance-duration-validation.xml", () -> {
					//given
					BootstrapConfiguration bootstrapConfiguration = ValidatorUtil.getConfiguration()
							.getBootstrapConfiguration();

					//then
					assertEquals(
							bootstrapConfiguration.getProperties().get( HibernateValidatorConfiguration.TEMPORAL_VALIDATION_TOLERANCE ),
							"123456"
					);

				}
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-707")
	public void shouldReturnDefaultExecutableTypesForValidationXmlWithoutTypesGiven() {
		validationXmlTestHelper.runWithCustomValidationXml(
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
		validationXmlTestHelper.runWithCustomValidationXml(
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
			expectedExceptionsMessageRegExp = "HV000122: Unsupported schema version for META-INF/validation.xml: 1\\.2\\."
	)
	public void shouldFailToLoadValidationXmlWithUnsupportedVersion() {
		validationXmlTestHelper.runWithCustomValidationXml(
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
		validationXmlTestHelper.runWithCustomValidationXml(
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

		assertThat( violations ).containsOnlyViolations(
				violationOf( NotNull.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1534")
	public void test_constraint_is_applied_to_inherited_getter() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "hv-1534-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		Parent parent = new Parent( null );

		Set<ConstraintViolation<Parent>> parentViolations = validator.validate( parent );

		assertNoViolations( parentViolations );

		Child child = new Child( null );

		Set<ConstraintViolation<Child>> childViolations = validator.validate( child );

		assertThat( childViolations ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "parentAttribute" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1534")
	public void test_constraint_is_applied_to_type_argument_of_inherited_getter() {
		final Configuration<?> configuration = ValidatorUtil.getConfiguration();
		configuration.addMapping( XmlMappingTest.class.getResourceAsStream( "hv-1534-mapping.xml" ) );

		final ValidatorFactory validatorFactory = configuration.buildValidatorFactory();
		final Validator validator = validatorFactory.getValidator();

		Parent parent = new Parent( "someValue" );
		parent.addToListAttribute( null );

		Set<ConstraintViolation<Parent>> parentViolations = validator.validate( parent );

		assertNoViolations( parentViolations );

		Child child = new Child( "someValue" );
		child.addToListAttribute( null );

		Set<ConstraintViolation<Child>> childViolations = validator.validate( child );

		assertThat( childViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
					.withPropertyPath(
						pathWith()
							.property( "parentListAttribute" )
							.containerElement( "<list element>", true, null, 0, List.class, 0 ) )
		);
	}

}
