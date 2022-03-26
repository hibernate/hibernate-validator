/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.records;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.Assert;
import org.testng.annotations.Test;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

/**
 * @author Jan Schatteman
 */
public class RecordConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testRecord() {
		PersonRecord r = new PersonRecord( "David", 15 );
		Set<ConstraintViolation<PersonRecord>> violations = validator.validate( r );
		assertNoViolations( violations );

		r = new PersonRecord( null, 15 );
		violations = validator.validate( r );
		assertThat( violations ).containsOnlyViolations( violationOf( NotBlank.class ).withProperty( "name" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( NotBlank.class ).withMessage( "Name cannot be null or empty" ) );

		r = new PersonRecord( "", 15 );
		violations = validator.validate( r );
		assertThat( violations ).containsOnlyViolations( violationOf( NotBlank.class ).withProperty( "name" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( NotBlank.class ).withMessage( "Name cannot be null or empty" ) );

		r = new PersonRecord( " ", 15 );
		violations = validator.validate( r );
		assertThat( violations ).containsOnlyViolations( violationOf( NotBlank.class ).withProperty( "name" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( NotBlank.class ).withMessage( "Name cannot be null or empty" ) );

		r = new PersonRecord( "David", 0 );
		 violations = validator.validate( r );
		assertThat( violations ).containsOnlyViolations( violationOf( Positive.class ).withProperty( "age" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( Positive.class ).withMessage( "Age has to be a strictly positive integer" ) );

		r = new PersonRecord( "David", -15 );
		violations = validator.validate( r );
		assertThat( violations ).containsOnlyViolations( violationOf( Positive.class ).withProperty( "age" ) );
		assertThat( violations ).containsOnlyViolations( violationOf( Positive.class ).withMessage( "Age has to be a strictly positive integer" ) );
	}

	@Test
	public void testExplicitConstructorRecord() {
		try {
			new ConstructorValidationRecord( "David", 15 );
		}
		catch (ConstraintViolationException e) {
			Assert.fail( "No violations expected" );
		}

		try {
			new ConstructorValidationRecord( null, 15 );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations( violationOf( NotBlank.class ).withMessage( "Name cannot be null or empty" ) );
		}

		try {
			new ConstructorValidationRecord( "", 15 );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations( violationOf( NotBlank.class ).withMessage( "Name cannot be null or empty" ) );
		}

		try {
			new ConstructorValidationRecord( " ", 15 );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations( violationOf( NotBlank.class ).withMessage( "Name cannot be null or empty" ) );
		}

		try {
			new ConstructorValidationRecord( "David", 0 );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations( violationOf( Positive.class ).withMessage( "Age has to be a strictly positive integer" ) );
		}

		try {
			new ConstructorValidationRecord( "David", -15 );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations( violationOf( Positive.class ).withMessage( "Age has to be a strictly positive integer" ) );
		}
	}

	@Test
	public void testCompactConstructorRecord() {
		try {
			new CompactConstructorValidationRecord( "David" );
		}
		catch (ConstraintViolationException e) {
			Assert.fail( "No violations expected" );
		}

		try {
			new CompactConstructorValidationRecord( null );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations( violationOf( NotBlank.class ).withMessage( "Name cannot be null or empty" ) );
		}

		try {
			new CompactConstructorValidationRecord( "" );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations( violationOf( NotBlank.class ).withMessage( "Name cannot be null or empty" ) );
		}

		try {
			new CompactConstructorValidationRecord( " " );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations( violationOf( NotBlank.class ).withMessage( "Name cannot be null or empty" ) );
		}
	}

	@Test
	public void testRecordMethodValidation() {
		try {
			new MethodValidationRecord( 50 );
		}
		catch (ConstraintViolationException e) {
			assertThat( e.getConstraintViolations() ).containsOnlyViolations( violationOf( Positive.class ) );
		}
	}

	private record PersonRecord(@NotBlank(message = "Name cannot be null or empty") String name, @Positive(message = "Age has to be a strictly positive integer") int age) {
	}

	private record ConstructorValidationRecord(String name, int age) implements ConstructorValidator {
		private ConstructorValidationRecord(@NotBlank(message = "Name cannot be null or empty") String name, @Positive(message = "Age has to be a strictly positive integer") int age) {
			validate( name, age );
			this.name = name;
			this.age = age;
		}
	}

	private record CompactConstructorValidationRecord(
			@NotBlank(message = "Name cannot be null or empty") String name) implements ConstructorValidator {
		private CompactConstructorValidationRecord {
			validate( name );
		}
	}

	private record MethodValidationRecord(int age) implements MethodValidator {
		private MethodValidationRecord(@Positive int age) {
			this.age = age;
			doSomethingSilly( age - 100 );
		}

		public void doSomethingSilly(@Positive int arg) {
			validate( this, new Object() { }.getClass().getEnclosingMethod(), arg );
		}
	}

	private interface ConstructorValidator {
		default void validate(Object ... args) {
			Validator v = ValidatorUtil.getValidator();
			Constructor c = getClass().getDeclaredConstructors()[0];
			Set<ConstraintViolation<?>> violations = v.forExecutables().validateConstructorParameters( c, args );
			if ( !violations.isEmpty() ) {
				String message = violations.stream()
						.map( ConstraintViolation::getMessage )
						.collect( Collectors.joining( ";" ) );
				throw new ConstraintViolationException( message, violations );
			}
		}
	}

	private interface MethodValidator {
		default void validate(Object instance, Method m, Object ... args) {
			Validator v = ValidatorUtil.getValidator();
			Set<ConstraintViolation<Object>> violations = v.forExecutables().validateParameters( instance, m, args );
			if ( !violations.isEmpty() ) {
				String message = violations.stream()
						.map( ConstraintViolation::getMessage )
						.collect( Collectors.joining( ";" ) );
				throw new ConstraintViolationException( message, violations );
			}
		}
	}
}
