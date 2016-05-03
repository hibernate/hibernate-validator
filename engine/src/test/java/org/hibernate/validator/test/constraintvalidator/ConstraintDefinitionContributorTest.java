/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraintvalidator;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ServiceLoaderBasedConstraintMappingContributor;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test related to constraint validator discovery.
 *
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = "HV-828")
public class ConstraintDefinitionContributorTest {

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = getValidator();
	}

	@Test
	public void constraint_definitions_can_be_configured_via_service_loader() {
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, MustMatch.class );
	}

	@Test()
	public void service_based_constraint_definition_contributor_is_configured_per_default() {
		HibernateValidatorConfiguration hibernateValidatorConfiguration = ValidatorUtil.getConfiguration();

		assertNotNull(
				hibernateValidatorConfiguration.getDefaultConstraintMappingContributor(),
				"There should be a default contributor"
		);
		assertTrue(
				hibernateValidatorConfiguration.getDefaultConstraintMappingContributor() instanceof ServiceLoaderBasedConstraintMappingContributor
		);
	}

	@Test
	@TestForIssue( jiraKey = "HV-953")
	public void constraints_defined_via_constraint_definition_contributor_can_have_default_message() {
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );
		assertCorrectConstraintViolationMessages( constraintViolations, "MustMatch default message" );
	}

	@Test
	@TestForIssue( jiraKey = "HV-953")
	public void user_can_override_default_message_of_constraint_definition_contributor() {
		Set<ConstraintViolation<Quz>> constraintViolations = validator.validate( new Quz() );
		assertCorrectConstraintViolationMessages( constraintViolations, "MustNotMatch user message" );
	}

	public class Foo {
		// constraint validator defined in service file!
		@MustMatch("Foo")
		String getFoo() {
			return "Bar";
		}
	}

	public class Quz {
		// constraint validator defined in service file!
		@MustNotMatch("Foo")
		String getFoo() {
			return "Foo";
		}
	}
}


