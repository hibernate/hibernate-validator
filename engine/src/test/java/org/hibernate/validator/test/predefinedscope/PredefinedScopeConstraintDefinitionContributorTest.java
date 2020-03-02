/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.predefinedscope;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.HashSet;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.PredefinedScopeHibernateValidator;
import org.hibernate.validator.test.constraintvalidator.MustMatch;
import org.hibernate.validator.test.constraintvalidator.MustNotMatch;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test related to constraint validator discovery in the predefined scope case.
 *
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-1721")
public class PredefinedScopeConstraintDefinitionContributorTest {

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = getValidator();
		Set<Class<?>> beanMetaDataToInitialize = new HashSet<>();
		beanMetaDataToInitialize.add( Foo.class );
		beanMetaDataToInitialize.add( Quz.class );

		ValidatorFactory validatorFactory = Validation.byProvider( PredefinedScopeHibernateValidator.class )
				.configure()
				.initializeBeanMetaData( beanMetaDataToInitialize )
				.buildValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	@Test
	public void constraint_definitions_can_be_configured_via_service_loader() {
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( MustMatch.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-953")
	public void constraints_defined_via_constraint_definition_contributor_can_have_default_message() {
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( MustMatch.class ).withMessage( "MustMatch default message" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-953")
	public void user_can_override_default_message_of_constraint_definition_contributor() {
		Set<ConstraintViolation<Quz>> constraintViolations = validator.validate( new Quz() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( MustNotMatch.class ).withMessage( "MustNotMatch user message" )
		);
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
