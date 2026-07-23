/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Stream;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.ISBNDef;
import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.internal.constraintvalidators.hv.ISBNValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A set of tests for {@link ISBN} constraint validator ({@link ISBNValidator}), which
 * make sure that validation is performed correctly.
 *
 * @author Marko Bekhta
 */
public class ISBNValidatorTest {

	private ISBNValidator validator;

	@BeforeEach
	public void setUp() throws Exception {
		validator = new ISBNValidator();
	}

	@ParameterizedTest
	@MethodSource("validISBN10Data")
	public void validISBN10(String isbn) throws Exception {
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN_10 ) );

		assertValidISBN( isbn );
	}

	private static Stream<Arguments> validISBN10Data() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "99921-58-10-7" ),
				Arguments.of( "9971-5-0210-0" ),
				Arguments.of( "960-425-059-0" ),
				Arguments.of( "80-902734-1-6" ),
				Arguments.of( "0-9752298-0-X" ),
				Arguments.of( "0-85131-041-9" ),
				Arguments.of( "0-684-84328-5" ),
				Arguments.of( "1-84356-028-3" ),
				Arguments.of( "3-598-21500-2" ),
				Arguments.of( "3-598-21501-0" ),
				Arguments.of( "3-598-21502-9" ),
				Arguments.of( "3-598-21503-7" ),
				Arguments.of( "3-598-21504-5" ),
				Arguments.of( "3-598-21505-3" ),
				Arguments.of( "3-598-21506-1" ),
				Arguments.of( "3-598-21507-X" ),
				Arguments.of( "3-598-21508-8" ),
				Arguments.of( "3-598-21509-6" ),
				Arguments.of( "3-598-21510-X" ),
				Arguments.of( "3-598-21511-8" ),
				Arguments.of( "3-598-21512-6" ),
				Arguments.of( "3-598-21513-4" ),
				Arguments.of( "3-598-21514-2" ),
				Arguments.of( "3-598-21515-0" ),
				Arguments.of( "3-598-21516-9" ),
				Arguments.of( "3-598-21517-7" ),
				Arguments.of( "3-598-21518-5" ),
				Arguments.of( "3-598-21519-3" ),
				Arguments.of( "3-598-21520-7" ),
				Arguments.of( "3-598-21521-5" ),
				Arguments.of( "3-598-21522-3" ),
				Arguments.of( "3-598-21523-1" ),
				Arguments.of( "3-598-21524-X" ),
				Arguments.of( "3-598-21525-8" ),
				Arguments.of( "3-598-21526-6" ),
				Arguments.of( "3-598-21527-4" ),
				Arguments.of( "3-598-21528-2" ),
				Arguments.of( "3-598-21529-0" ),
				Arguments.of( "3-598-21530-4" ),
				Arguments.of( "3-598-21531-2" ),
				Arguments.of( "3-598-21532-0" ),
				Arguments.of( "3-598-21533-9" ),
				Arguments.of( "3-598-21534-7" ),
				Arguments.of( "3-598-21535-5" ),
				Arguments.of( "3-598-21536-3" ),
				Arguments.of( "3-598-21537-1" ),
				Arguments.of( "3-598-21538-X" ),
				Arguments.of( "3-598-21539-8" ),
				Arguments.of( "3-598-21540-1" ),
				Arguments.of( "3-598-21541-X" ),
				Arguments.of( "3-598-21542-8" ),
				Arguments.of( "3-598-21543-6" ),
				Arguments.of( "3-598-21544-4" ),
				Arguments.of( "3-598-21545-2" ),
				Arguments.of( "3-598-21546-0" ),
				Arguments.of( "3-598-21547-9" ),
				Arguments.of( "3-598-21548-7" ),
				Arguments.of( "3-598-21549-5" ),
				Arguments.of( "3-598-21550-9" ),
				Arguments.of( "3-598-21551-7" ),
				Arguments.of( "3-598-21552-5" ),
				Arguments.of( "3-598-21553-3" ),
				Arguments.of( "3-598-21554-1" ),
				Arguments.of( "3-598-21555-X" ),
				Arguments.of( "3-598-21556-8" ),
				Arguments.of( "3-598-21557-6" ),
				Arguments.of( "3-598-21558-4" ),
				Arguments.of( "3-598-21559-2" ),
				Arguments.of( "3-598-21560-6" ),
				Arguments.of( "3-598-21561-4" ),
				Arguments.of( "3-598-21562-2" ),
				Arguments.of( "3-598-21563-0" ),
				Arguments.of( "3-598-21564-9" ),
				Arguments.of( "3-598-21565-7" ),
				Arguments.of( "3-598-21566-5" ),
				Arguments.of( "3-598-21567-3" ),
				Arguments.of( "3-598-21568-1" ),
				Arguments.of( "3-598-21569-X" ),
				Arguments.of( "3-598-21570-3" ),
				Arguments.of( "3-598-21571-1" ),
				Arguments.of( "3-598-21572-X" ),
				Arguments.of( "3-598-21573-8" ),
				Arguments.of( "3-598-21574-6" ),
				Arguments.of( "3-598-21575-4" ),
				Arguments.of( "3-598-21576-2" ),
				Arguments.of( "3-598-21577-0" ),
				Arguments.of( "3-598-21578-9" ),
				Arguments.of( "3-598-21579-7" ),
				Arguments.of( "3-598-21580-0" ),
				Arguments.of( "3-598-21581-9" ),
				Arguments.of( "3-598-21582-7" ),
				Arguments.of( "3-598-21583-5" ),
				Arguments.of( "3-598-21584-3" ),
				Arguments.of( "3-598-21585-1" ),
				Arguments.of( "3-598-21586-X" ),
				Arguments.of( "3-598-21587-8" ),
				Arguments.of( "3-598-21588-6" ),
				Arguments.of( "3-598-21589-4" ),
				Arguments.of( "3-598-21590-8" ),
				Arguments.of( "3-598-21591-6" ),
				Arguments.of( "3-598-21592-4" ),
				Arguments.of( "3-598-21593-2" ),
				Arguments.of( "3-598-21594-0" ),
				Arguments.of( "3-598-21595-9" ),
				Arguments.of( "3-598-21596-7" ),
				Arguments.of( "3-598-21597-5" ),
				Arguments.of( "3-598-21598-3" ),
				Arguments.of( "3-598-21599-1" )
		);
	}

	@ParameterizedTest
	@MethodSource("invalidISBN10Data")
	public void invalidISBN10(String isbn) throws Exception {
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN_10 ) );

		assertInvalidISBN( isbn );
	}

	private static Stream<Arguments> invalidISBN10Data() {
		return Stream.of(
				// invalid check-digit
				Arguments.of( "99921-58-10-8" ),
				Arguments.of( "9971-5-0210-1" ),
				Arguments.of( "960-425-059-2" ),
				Arguments.of( "80-902734-1-8" ),
				Arguments.of( "0-9752298-0-3" ),
				Arguments.of( "0-85131-041-X" ),
				Arguments.of( "0-684-84328-7" ),
				Arguments.of( "1-84356-028-1" ),
				// invalid length
				Arguments.of( "" ),
				Arguments.of( "978-0-5" ),
				Arguments.of( "978-0-55555555555555" )
		);
	}

	@ParameterizedTest
	@MethodSource("validISBN13Data")
	public void validISBN13(String isbn) throws Exception {
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN_13 ) );

		assertValidISBN( isbn );
	}

	private static Stream<Arguments> validISBN13Data() {
		return Stream.of(
				Arguments.of( (String) null ),
				Arguments.of( "978-123-456-789-7" ),
				Arguments.of( "978-91-983989-1-5" ),
				Arguments.of( "978-988-785-411-1" ),
				Arguments.of( "978-1-56619-909-4" ),
				Arguments.of( "978-1-4028-9462-6" ),
				Arguments.of( "978-0-85131-041-1" ),
				Arguments.of( "978-0-684-84328-5" ),
				Arguments.of( "978-1-84356-028-9" ),
				Arguments.of( "978-0-54560-495-6" ),
				Arguments.of( "978-3-598-21500-1" ),
				Arguments.of( "978-3-598-21501-8" ),
				Arguments.of( "978-3-598-21502-5" ),
				Arguments.of( "978-3-598-21503-2" ),
				Arguments.of( "978-3-598-21504-9" ),
				Arguments.of( "978-3-598-21505-6" ),
				Arguments.of( "978-3-598-21506-3" ),
				Arguments.of( "978-3-598-21507-0" ),
				Arguments.of( "978-3-598-21508-7" ),
				Arguments.of( "978-3-598-21509-4" ),
				Arguments.of( "978-3-598-21510-0" ),
				Arguments.of( "978-3-598-21511-7" ),
				Arguments.of( "978-3-598-21512-4" ),
				Arguments.of( "978-3-598-21513-1" ),
				Arguments.of( "978-3-598-21514-8" ),
				Arguments.of( "978-3-598-21515-5" ),
				Arguments.of( "978-3-598-21516-2" ),
				Arguments.of( "978-3-598-21517-9" ),
				Arguments.of( "978-3-598-21518-6" ),
				Arguments.of( "978-3-598-21519-3" ),
				Arguments.of( "978-3-598-21520-9" ),
				Arguments.of( "978-3-598-21521-6" ),
				Arguments.of( "978-3-598-21522-3" ),
				Arguments.of( "978-3-598-21523-0" ),
				Arguments.of( "978-3-598-21524-7" ),
				Arguments.of( "978-3-598-21525-4" ),
				Arguments.of( "978-3-598-21526-1" ),
				Arguments.of( "978-3-598-21527-8" ),
				Arguments.of( "978-3-598-21528-5" ),
				Arguments.of( "978-3-598-21529-2" ),
				Arguments.of( "978-3-598-21530-8" ),
				Arguments.of( "978-3-598-21531-5" ),
				Arguments.of( "978-3-598-21532-2" ),
				Arguments.of( "978-3-598-21533-9" ),
				Arguments.of( "978-3-598-21534-6" ),
				Arguments.of( "978-3-598-21535-3" ),
				Arguments.of( "978-3-598-21536-0" ),
				Arguments.of( "978-3-598-21537-7" ),
				Arguments.of( "978-3-598-21538-4" ),
				Arguments.of( "978-3-598-21539-1" ),
				Arguments.of( "978-3-598-21540-7" ),
				Arguments.of( "978-3-598-21541-4" ),
				Arguments.of( "978-3-598-21542-1" ),
				Arguments.of( "978-3-598-21543-8" ),
				Arguments.of( "978-3-598-21544-5" ),
				Arguments.of( "978-3-598-21545-2" ),
				Arguments.of( "978-3-598-21546-9" ),
				Arguments.of( "978-3-598-21547-6" ),
				Arguments.of( "978-3-598-21548-3" ),
				Arguments.of( "978-3-598-21549-0" ),
				Arguments.of( "978-3-598-21550-6" ),
				Arguments.of( "978-3-598-21551-3" ),
				Arguments.of( "978-3-598-21552-0" ),
				Arguments.of( "978-3-598-21553-7" ),
				Arguments.of( "978-3-598-21554-4" ),
				Arguments.of( "978-3-598-21555-1" ),
				Arguments.of( "978-3-598-21556-8" ),
				Arguments.of( "978-3-598-21557-5" ),
				Arguments.of( "978-3-598-21558-2" ),
				Arguments.of( "978-3-598-21559-9" ),
				Arguments.of( "978-3-598-21560-5" ),
				Arguments.of( "978-3-598-21561-2" ),
				Arguments.of( "978-3-598-21562-9" ),
				Arguments.of( "978-3-598-21563-6" ),
				Arguments.of( "978-3-598-21564-3" ),
				Arguments.of( "978-3-598-21565-0" ),
				Arguments.of( "978-3-598-21566-7" ),
				Arguments.of( "978-3-598-21567-4" ),
				Arguments.of( "978-3-598-21568-1" ),
				Arguments.of( "978-3-598-21569-8" ),
				Arguments.of( "978-3-598-21570-4" ),
				Arguments.of( "978-3-598-21571-1" ),
				Arguments.of( "978-3-598-21572-8" ),
				Arguments.of( "978-3-598-21573-5" ),
				Arguments.of( "978-3-598-21574-2" ),
				Arguments.of( "978-3-598-21575-9" ),
				Arguments.of( "978-3-598-21576-6" ),
				Arguments.of( "978-3-598-21577-3" ),
				Arguments.of( "978-3-598-21578-0" ),
				Arguments.of( "978-3-598-21579-7" ),
				Arguments.of( "978-3-598-21580-3" ),
				Arguments.of( "978-3-598-21581-0" ),
				Arguments.of( "978-3-598-21582-7" ),
				Arguments.of( "978-3-598-21583-4" ),
				Arguments.of( "978-3-598-21584-1" ),
				Arguments.of( "978-3-598-21585-8" ),
				Arguments.of( "978-3-598-21586-5" ),
				Arguments.of( "978-3-598-21587-2" ),
				Arguments.of( "978-3-598-21588-9" ),
				Arguments.of( "978-3-598-21589-6" ),
				Arguments.of( "978-3-598-21590-2" ),
				Arguments.of( "978-3-598-21591-9" ),
				Arguments.of( "978-3-598-21592-6" ),
				Arguments.of( "978-3-598-21593-3" ),
				Arguments.of( "978-3-598-21594-0" ),
				Arguments.of( "978-3-598-21595-7" ),
				Arguments.of( "978-3-598-21596-4" ),
				Arguments.of( "978-3-598-21597-1" ),
				Arguments.of( "978-3-598-21598-8" ),
				Arguments.of( "978-3-598-21599-5" )
		);
	}

	@ParameterizedTest
	@MethodSource("invalidISBN13Data")
	public void invalidISBN13(String isbn) throws Exception {
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN_13 ) );

		assertInvalidISBN( isbn );
	}

	private static Stream<Arguments> invalidISBN13Data() {
		return Stream.of(
				// invalid check-digit
				Arguments.of( "978-123-456-789-6" ),
				Arguments.of( "978-91-983989-1-4" ),
				Arguments.of( "978-988-785-411-2" ),
				Arguments.of( "978-1-56619-909-1" ),
				Arguments.of( "978-1-4028-9462-0" ),
				Arguments.of( "978-0-85131-041-5" ),
				Arguments.of( "978-0-684-84328-1" ),
				Arguments.of( "978-1-84356-028-1" ),
				Arguments.of( "978-0-54560-495-4" ),
				// invalid length
				Arguments.of( "" ),
				Arguments.of( "978-0-54560-4" ),
				Arguments.of( "978-0-55555555555555" )
		);
	}

	@ParameterizedTest
	@MethodSource("validISBNAnyData")
	@TestForIssue(jiraKey = "HV-1707")
	public void validISBNAny(String isbn) {
		validator.initialize( initializeAnnotation( ISBN.Type.ANY ) );
		assertValidISBN( isbn );
	}

	private static Stream<Arguments> validISBNAnyData() {
		return Stream.of(
				Arguments.of( (String) null ),
				// ISBN10
				Arguments.of( "99921-58-10-7" ),
				Arguments.of( "9971-5-0210-0" ),
				Arguments.of( "960-425-059-0" ),
				Arguments.of( "80-902734-1-6" ),
				Arguments.of( "0-9752298-0-X" ),
				// ISBN13
				Arguments.of( "978-123-456-789-7" ),
				Arguments.of( "978-91-983989-1-5" ),
				Arguments.of( "978-988-785-411-1" ),
				Arguments.of( "978-1-56619-909-4" ),
				Arguments.of( "978-1-4028-9462-6" ),
				Arguments.of( "978-0-85131-041-1" )
		);
	}

	@ParameterizedTest
	@MethodSource("invalidISBNAnyData")
	@TestForIssue(jiraKey = "HV-1707")
	public void invalidISBNAny(String isbn) {
		validator.initialize( initializeAnnotation( ISBN.Type.ANY ) );
		assertInvalidISBN( isbn );
	}

	private static Stream<Arguments> invalidISBNAnyData() {
		return Stream.of(
				// invalid cases
				Arguments.of( "978-0-85131-041-0" ),
				Arguments.of( "80-902734-1-8" ),
				Arguments.of( "978-0-85131-0401-0" ),
				Arguments.of( "80-902734-10-8" ),
				// incorrect length
				Arguments.of( "978-0-85" ),
				Arguments.of( "80-902734-1-877777" ),
				Arguments.of( "978-0-85131-0401-00074" ),
				Arguments.of( "80-902734-1" )
		);
	}

	@Test
	public void testProgrammaticDefinition() throws Exception {
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Book.class )
				.field( "isbn" )
				.constraint( new ISBNDef().type( ISBN.Type.ISBN_13 ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Book>> constraintViolations = validator.validate( new Book( "978-0-54560-495-6" ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new Book( "978-0-54560-495-7" ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( ISBN.class )
		);
	}

	private void assertValidISBN(String isbn) {
		assertTrue( validator.isValid( isbn, null ), isbn + " should be a valid ISBN" );
	}

	private void assertInvalidISBN(String isbn) {
		assertFalse( validator.isValid( isbn, null ), isbn + " should be an invalid ISBN" );
	}

	private ISBN initializeAnnotation(ISBN.Type type) {
		ConstraintAnnotationDescriptor.Builder<ISBN> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( ISBN.class );
		descriptorBuilder.setAttribute( "type", type );
		return descriptorBuilder.build().getAnnotation();
	}

	private static class Book {

		private final String isbn;

		private Book(String isbn) {
			this.isbn = isbn;
		}
	}
}
