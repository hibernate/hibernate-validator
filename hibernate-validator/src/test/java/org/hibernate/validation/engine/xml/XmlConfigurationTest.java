// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine.xml;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import static org.testng.Assert.assertFalse;
import org.testng.annotations.Test;

import org.hibernate.validation.util.TestUtil;
import static org.hibernate.validation.util.TestUtil.assertNumberOfViolations;
import static org.hibernate.validation.util.TestUtil.getValidator;
import static org.hibernate.validation.util.TestUtil.getValidatorWithCustomConfiguration;

/**
 * @author Hardy Ferentschik
 */
public class XmlConfigurationTest {

	@Test
	public void testClassConstraintDefinedInXml() {
		Validator validator = getValidator();

		User user = new User();
		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 1 );
		TestUtil.assertConstraintViolation( constraintViolations.iterator().next(), "Message from xml" );

		user.setConsistent( true );
		constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void testPropertyConstraintDefinedInXml() {
		Validator validator = getValidator();

		User user = new User();
		user.setConsistent( true );
		user.setFirstname( "Wolfeschlegelsteinhausenbergerdorff" );

		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 1 );
		TestUtil.assertConstraintViolation( constraintViolations.iterator().next(), "Size is limited!" );

		user.setFirstname( "Wolfgang" );
		constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void testFieldConstraintDefinedInXml() {
		Validator validator = getValidator();

		User user = new User();
		user.setConsistent( true );
		user.setFirstname( "Wolfgang" );
		user.setLastname( "doe" );

		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 1 );
		TestUtil.assertConstraintViolation(
				constraintViolations.iterator().next(), "Last name has to start with with a capital letter."
		);

		user.setLastname( "Doe" );
		constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void testAnnotationDefinedConstraintApplies() {
		Validator validator = getValidator();

		User user = new User();
		user.setConsistent( true );
		user.setPhoneNumber( "police" );

		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 1 );
		TestUtil.assertConstraintViolation(
				constraintViolations.iterator().next(),
				"A phone number can only contain numbers, whitespaces and dashes."
		);

		user.setPhoneNumber( "112" );
		constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	public void testCascadingConfiguredInXml() {
		Validator validator = getValidator();

		User user = new User();
		user.setConsistent( true );
		CreditCard card = new CreditCard();
		card.setNumber( "not a number" );
		user.setCreditcard( card );

		Set<ConstraintViolation<User>> constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 1 );
		TestUtil.assertConstraintViolation(
				constraintViolations.iterator().next(),
				"Not a credit casrd number."
		);

		card.setNumber( "1234567890" );
		constraintViolations = validator.validate( user );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test(expectedExceptions = ValidationException.class)
	public void testInvalidValidationXml() {
		getValidatorWithCustomConfiguration( "META-INF/validation-invalid-xml.xml" );
	}

	/**
	 * HV-159
	 */
	@Test
	public void testNoDefinedConstraints() {
		Validator validator = getValidatorWithCustomConfiguration( "org/hibernate/validation/engine/xml/validation.xml" );
		assertFalse(
				validator.getConstraintsForClass( Order.class ).isBeanConstrained(), "Bean should be unsonstrained"
		);
	}
}
