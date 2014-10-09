/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.constraintvalidator;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.UnexpectedTypeException;
import javax.validation.Validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.internal.engine.ServiceLoaderBasedConstraintDefinitionContributor;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
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

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030.*")
	public void validating_constraint_without_registered_constraint_validator_throws_exception() {
		validator.validate( new Bar() );
	}

	@Test()
	public void service_based_constraint_definition_contributor_is_configured_per_default() {
		HibernateValidatorConfiguration hibernateValidatorConfiguration = ValidatorUtil.getConfiguration();

		assertNotNull(
				hibernateValidatorConfiguration.getDefaultConstraintDefinitionContributor(),
				"There should be a default contributor"
		);
		assertTrue(
				hibernateValidatorConfiguration.getDefaultConstraintDefinitionContributor() instanceof ServiceLoaderBasedConstraintDefinitionContributor
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "HV000116.*")
	public void null_cannot_be_passed_to_add_constraint_definition_contributor() {
		validator = ValidatorUtil.getConfiguration()
				.addConstraintDefinitionContributor( null )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Bar>> constraintViolations = validator.validate( new Bar() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, AcmeConstraint.class );
	}

	@Test
	public void constraint_definition_contributor_instance_can_be_programmatically_registered() {
		validator = ValidatorUtil.getConfiguration()
				.addConstraintDefinitionContributor( new AcmeConstraintDefinitionContributor( true ) )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Bar>> constraintViolations = validator.validate( new Bar() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, AcmeConstraint.class );
	}

	@Test
	public void constraint_definition_contributor_can_be_configured_via_property() {
		validator = ValidatorUtil.getConfiguration()
				.addProperty(
						HibernateValidatorConfiguration.CONSTRAINT_DEFINITION_CONTRIBUTORS,
						AcmeConstraintDefinitionContributor.class.getName()
				)
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Bar>> constraintViolations = validator.validate( new Bar() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, AcmeConstraint.class );
	}

	@Test
	public void constraint_definition_contributor_can_disable_default_constraint_validators() {
		validator = ValidatorUtil.getConfiguration()
				.addConstraintDefinitionContributor( new AcmeConstraintDefinitionContributor( false ) )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Baz>> constraintViolations = validator.validate( new Baz() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, AcmeConstraintWithDefaultValidator.class );
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000150.*")
	public void having_a_default_constraint_validator_and_a_contributed_constraint_validator_is_invalid() {
		validator = ValidatorUtil.getConfiguration()
				.addConstraintDefinitionContributor( new AcmeConstraintDefinitionContributor( true ) )
				.buildValidatorFactory()
				.getValidator();

		validator.validate( new Baz() );
	}

	public class Foo {
		// constraint validator defined in service file!
		@MustMatch("Foo")
		String getFoo() {
			return "Foobar";
		}
	}

	public class Bar {
		@AcmeConstraint
		String getBaz() {
			return "Boom";
		}
	}

	public class Baz {
		@AcmeConstraintWithDefaultValidator
		String getBaz() {
			return "Boom";
		}
	}
}


