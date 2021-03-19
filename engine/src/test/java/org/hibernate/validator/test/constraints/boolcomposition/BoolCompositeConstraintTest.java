/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.test.constraints.boolcomposition;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

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

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( PatternOrSize.class ).withProperty( "name" ),
				violationOf( NotNullAndSize.class ).withProperty( "name" ),
				violationOf( TemporarySSN.class ).withProperty( "ssn" ),
				violationOf( SSN.class ).withProperty( "ssn" )
		);

		constraintViolations = currentValidator.validate(
				new Person( "G", "Gerhard" )
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( PatternOrSize.class ).withProperty( "nickName" ),
				violationOf( TemporarySSN.class ).withProperty( "ssn" ),
				violationOf( SSN.class ).withProperty( "ssn" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-390")
	public void testCorrectAnnotationTypeWithBoolAnd() {
		Validator currentValidator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
				new Person( "G", "K" )
		);

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( PatternOrSize.class ).withProperty( "nickName" ),
				violationOf( NotNullAndSize.class ).withProperty( "name" ),
				violationOf( TemporarySSN.class ).withProperty( "ssn" ),
				violationOf( SSN.class ).withProperty( "ssn" )
		);

		constraintViolations = currentValidator.validate(
				new Person( "L", "G" )
		);
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNullAndSize.class ).withProperty( "name" ),
				violationOf( PatternOrSize.class ).withProperty( "name" ),
				violationOf( PatternOrSize.class ).withProperty( "nickName" ),
				violationOf( TemporarySSN.class ).withProperty( "ssn" ),
				violationOf( SSN.class ).withProperty( "ssn" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-390")
	public void testCorrectAnnotationTypeWithBoolAllFalse() {
		Validator currentValidator = ValidatorUtil.getValidator();
		// Uses ALL_FALSE, OR, and AND. Checks that SSN works
		Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
				new Person( "NickName", "Name", "33333333333" )
		);

		assertNoViolations( constraintViolations );

		// Uses ALL_FALSE, OR, and AND. Checks that TemporarySSN works
		constraintViolations = currentValidator.validate(
				new Person( "NickName", "Name", "333333" )
		);

		assertNoViolations( constraintViolations );

		// Checks that two negations work
		constraintViolations = currentValidator.validate(
				new Person( "NickName", "Name", "333333", "12345678901" )
		);

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ExcludedSSNList.class ).withProperty( "anotherSsn" ),
				violationOf( IsBlank.class ).withProperty( "anotherSsn" )
		);

		// Checks that negation on a list works
		constraintViolations = currentValidator.validate(
				new Person( "NickName", "Name", "12345678901" )
		);

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ExcludedSSNList.class ).withProperty( "ssn" )
		);

		// Checks that all parts of an "or" ar reported
		constraintViolations = currentValidator.validate(
				new Person( "NickName", "Name", "12345678" )
		);

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( TemporarySSN.class ).withProperty( "ssn" ),
				violationOf( SSN.class ).withProperty( "ssn" )
		);
	}
}

