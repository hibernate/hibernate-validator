/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.hibernate.validator.test.constraints.boolcomposition;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

/**
 * @author Federico Mancini
 * @author Dag Hovland
 */
public class BoolCompositeConstraintTest {

	@Test
	@TestForIssue(jiraKey = "HV-390")
	public void testCorrectAnnotationTypeWithBoolOr() {
		Validator currentValidator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
				new Person( "K", "G" )
		);

		assertNumberOfViolations( constraintViolations, 4 );
		assertCorrectConstraintTypes(
				constraintViolations, PatternOrSize.class, NotNullAndSize.class, TemporarySSN.class, SSN.class
		);
		assertCorrectPropertyPaths( constraintViolations, "name", "name", "ssn", "ssn" );

		constraintViolations = currentValidator.validate(
				new Person( "G", "Gerhard" )
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectConstraintTypes( constraintViolations, PatternOrSize.class, TemporarySSN.class, SSN.class );
		assertCorrectPropertyPaths( constraintViolations, "nickName", "ssn", "ssn" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-390")
	public void testCorrectAnnotationTypeWithBoolAnd() {
		Validator currentValidator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
				new Person( "G", "K" )
		);

		assertNumberOfViolations( constraintViolations, 4 );
		assertCorrectConstraintTypes(
				constraintViolations, PatternOrSize.class, NotNullAndSize.class, TemporarySSN.class, SSN.class
		);
		assertCorrectPropertyPaths( constraintViolations, "name", "nickName", "ssn", "ssn" );

		constraintViolations = currentValidator.validate(
				new Person( "L", "G" )
		);
		assertNumberOfViolations( constraintViolations, 5 );
		assertCorrectConstraintTypes(
				constraintViolations,
				NotNullAndSize.class,
				PatternOrSize.class,
				PatternOrSize.class,
				TemporarySSN.class,
				SSN.class
		);
		assertCorrectPropertyPaths( constraintViolations, "name", "name", "nickName", "ssn", "ssn" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-390")
	public void testCorrectAnnotationTypeWithBoolAllFalse() {
		Validator currentValidator = ValidatorUtil.getValidator();
		// Uses ALL_FALSE, OR, and AND. Checks that SSN works
		Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
				new Person( "NickName", "Name", "33333333333" )
		);

		assertNumberOfViolations( constraintViolations, 0 );

		// Uses ALL_FALSE, OR, and AND. Checks that TemporarySSN works
		constraintViolations = currentValidator.validate(
				new Person( "NickName", "Name", "333333" )
		);

		assertNumberOfViolations( constraintViolations, 0 );

		// Checks that two negations work
		constraintViolations = currentValidator.validate(
				new Person( "NickName", "Name", "333333", "12345678901" )
		);

		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectConstraintTypes( constraintViolations, Blacklist.class, IsBlank.class );
		assertCorrectPropertyPaths( constraintViolations, "anotherSsn", "anotherSsn" );

		// Checks that negation on a list works
		constraintViolations = currentValidator.validate(
				new Person( "NickName", "Name", "12345678901" )
		);

		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, Blacklist.class );
		assertCorrectPropertyPaths( constraintViolations, "ssn" );

		// Checks that all parts of an "or" ar reported
		constraintViolations = currentValidator.validate(
				new Person( "NickName", "Name", "12345678" )
		);

		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectConstraintTypes( constraintViolations, TemporarySSN.class, SSN.class );
		assertCorrectPropertyPaths( constraintViolations, "ssn", "ssn" );
	}
}

