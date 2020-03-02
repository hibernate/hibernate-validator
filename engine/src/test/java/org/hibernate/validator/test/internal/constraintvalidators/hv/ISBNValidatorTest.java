/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A set of tests for {@link ISBN} constraint validator ({@link ISBNValidator}), which
 * make sure that validation is performed correctly.
 *
 * @author Marko Bekhta
 */
public class ISBNValidatorTest {

	private ISBNValidator validator;

	@BeforeMethod
	public void setUp() throws Exception {
		validator = new ISBNValidator();
	}

	@Test
	public void validISBN10() throws Exception {
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN_10 ) );

		assertValidISBN( null );
		assertValidISBN( "99921-58-10-7" );
		assertValidISBN( "9971-5-0210-0" );
		assertValidISBN( "960-425-059-0" );
		assertValidISBN( "80-902734-1-6" );
		assertValidISBN( "0-9752298-0-X" );
		assertValidISBN( "0-85131-041-9" );
		assertValidISBN( "0-684-84328-5" );
		assertValidISBN( "1-84356-028-3" );
		assertValidISBN( "3-598-21500-2" );
		assertValidISBN( "3-598-21501-0" );
		assertValidISBN( "3-598-21502-9" );
		assertValidISBN( "3-598-21503-7" );
		assertValidISBN( "3-598-21504-5" );
		assertValidISBN( "3-598-21505-3" );
		assertValidISBN( "3-598-21506-1" );
		assertValidISBN( "3-598-21507-X" );
		assertValidISBN( "3-598-21508-8" );
		assertValidISBN( "3-598-21509-6" );
		assertValidISBN( "3-598-21510-X" );
		assertValidISBN( "3-598-21511-8" );
		assertValidISBN( "3-598-21512-6" );
		assertValidISBN( "3-598-21513-4" );
		assertValidISBN( "3-598-21514-2" );
		assertValidISBN( "3-598-21515-0" );
		assertValidISBN( "3-598-21516-9" );
		assertValidISBN( "3-598-21517-7" );
		assertValidISBN( "3-598-21518-5" );
		assertValidISBN( "3-598-21519-3" );
		assertValidISBN( "3-598-21520-7" );
		assertValidISBN( "3-598-21521-5" );
		assertValidISBN( "3-598-21522-3" );
		assertValidISBN( "3-598-21523-1" );
		assertValidISBN( "3-598-21524-X" );
		assertValidISBN( "3-598-21525-8" );
		assertValidISBN( "3-598-21526-6" );
		assertValidISBN( "3-598-21527-4" );
		assertValidISBN( "3-598-21528-2" );
		assertValidISBN( "3-598-21529-0" );
		assertValidISBN( "3-598-21530-4" );
		assertValidISBN( "3-598-21531-2" );
		assertValidISBN( "3-598-21532-0" );
		assertValidISBN( "3-598-21533-9" );
		assertValidISBN( "3-598-21534-7" );
		assertValidISBN( "3-598-21535-5" );
		assertValidISBN( "3-598-21536-3" );
		assertValidISBN( "3-598-21537-1" );
		assertValidISBN( "3-598-21538-X" );
		assertValidISBN( "3-598-21539-8" );
		assertValidISBN( "3-598-21540-1" );
		assertValidISBN( "3-598-21541-X" );
		assertValidISBN( "3-598-21542-8" );
		assertValidISBN( "3-598-21543-6" );
		assertValidISBN( "3-598-21544-4" );
		assertValidISBN( "3-598-21545-2" );
		assertValidISBN( "3-598-21546-0" );
		assertValidISBN( "3-598-21547-9" );
		assertValidISBN( "3-598-21548-7" );
		assertValidISBN( "3-598-21549-5" );
		assertValidISBN( "3-598-21550-9" );
		assertValidISBN( "3-598-21551-7" );
		assertValidISBN( "3-598-21552-5" );
		assertValidISBN( "3-598-21553-3" );
		assertValidISBN( "3-598-21554-1" );
		assertValidISBN( "3-598-21555-X" );
		assertValidISBN( "3-598-21556-8" );
		assertValidISBN( "3-598-21557-6" );
		assertValidISBN( "3-598-21558-4" );
		assertValidISBN( "3-598-21559-2" );
		assertValidISBN( "3-598-21560-6" );
		assertValidISBN( "3-598-21561-4" );
		assertValidISBN( "3-598-21562-2" );
		assertValidISBN( "3-598-21563-0" );
		assertValidISBN( "3-598-21564-9" );
		assertValidISBN( "3-598-21565-7" );
		assertValidISBN( "3-598-21566-5" );
		assertValidISBN( "3-598-21567-3" );
		assertValidISBN( "3-598-21568-1" );
		assertValidISBN( "3-598-21569-X" );
		assertValidISBN( "3-598-21570-3" );
		assertValidISBN( "3-598-21571-1" );
		assertValidISBN( "3-598-21572-X" );
		assertValidISBN( "3-598-21573-8" );
		assertValidISBN( "3-598-21574-6" );
		assertValidISBN( "3-598-21575-4" );
		assertValidISBN( "3-598-21576-2" );
		assertValidISBN( "3-598-21577-0" );
		assertValidISBN( "3-598-21578-9" );
		assertValidISBN( "3-598-21579-7" );
		assertValidISBN( "3-598-21580-0" );
		assertValidISBN( "3-598-21581-9" );
		assertValidISBN( "3-598-21582-7" );
		assertValidISBN( "3-598-21583-5" );
		assertValidISBN( "3-598-21584-3" );
		assertValidISBN( "3-598-21585-1" );
		assertValidISBN( "3-598-21586-X" );
		assertValidISBN( "3-598-21587-8" );
		assertValidISBN( "3-598-21588-6" );
		assertValidISBN( "3-598-21589-4" );
		assertValidISBN( "3-598-21590-8" );
		assertValidISBN( "3-598-21591-6" );
		assertValidISBN( "3-598-21592-4" );
		assertValidISBN( "3-598-21593-2" );
		assertValidISBN( "3-598-21594-0" );
		assertValidISBN( "3-598-21595-9" );
		assertValidISBN( "3-598-21596-7" );
		assertValidISBN( "3-598-21597-5" );
		assertValidISBN( "3-598-21598-3" );
		assertValidISBN( "3-598-21599-1" );
	}

	@Test
	public void invalidISBN10() throws Exception {
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN_10 ) );

		// invalid check-digit
		assertInvalidISBN( "99921-58-10-8" );
		assertInvalidISBN( "9971-5-0210-1" );
		assertInvalidISBN( "960-425-059-2" );
		assertInvalidISBN( "80-902734-1-8" );
		assertInvalidISBN( "0-9752298-0-3" );
		assertInvalidISBN( "0-85131-041-X" );
		assertInvalidISBN( "0-684-84328-7" );
		assertInvalidISBN( "1-84356-028-1" );

		// invalid length
		assertInvalidISBN( "" );
		assertInvalidISBN( "978-0-5" );
		assertInvalidISBN( "978-0-55555555555555" );
	}

	@Test
	public void validISBN13() throws Exception {
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN_13 ) );

		assertValidISBN( null );
		assertValidISBN( "978-123-456-789-7" );
		assertValidISBN( "978-91-983989-1-5" );
		assertValidISBN( "978-988-785-411-1" );
		assertValidISBN( "978-1-56619-909-4" );
		assertValidISBN( "978-1-4028-9462-6" );
		assertValidISBN( "978-0-85131-041-1" );
		assertValidISBN( "978-0-684-84328-5" );
		assertValidISBN( "978-1-84356-028-9" );
		assertValidISBN( "978-0-54560-495-6" );
		assertValidISBN( "978-3-598-21500-1" );
		assertValidISBN( "978-3-598-21501-8" );
		assertValidISBN( "978-3-598-21502-5" );
		assertValidISBN( "978-3-598-21503-2" );
		assertValidISBN( "978-3-598-21504-9" );
		assertValidISBN( "978-3-598-21505-6" );
		assertValidISBN( "978-3-598-21506-3" );
		assertValidISBN( "978-3-598-21507-0" );
		assertValidISBN( "978-3-598-21508-7" );
		assertValidISBN( "978-3-598-21509-4" );
		assertValidISBN( "978-3-598-21510-0" );
		assertValidISBN( "978-3-598-21511-7" );
		assertValidISBN( "978-3-598-21512-4" );
		assertValidISBN( "978-3-598-21513-1" );
		assertValidISBN( "978-3-598-21514-8" );
		assertValidISBN( "978-3-598-21515-5" );
		assertValidISBN( "978-3-598-21516-2" );
		assertValidISBN( "978-3-598-21517-9" );
		assertValidISBN( "978-3-598-21518-6" );
		assertValidISBN( "978-3-598-21519-3" );
		assertValidISBN( "978-3-598-21520-9" );
		assertValidISBN( "978-3-598-21521-6" );
		assertValidISBN( "978-3-598-21522-3" );
		assertValidISBN( "978-3-598-21523-0" );
		assertValidISBN( "978-3-598-21524-7" );
		assertValidISBN( "978-3-598-21525-4" );
		assertValidISBN( "978-3-598-21526-1" );
		assertValidISBN( "978-3-598-21527-8" );
		assertValidISBN( "978-3-598-21528-5" );
		assertValidISBN( "978-3-598-21529-2" );
		assertValidISBN( "978-3-598-21530-8" );
		assertValidISBN( "978-3-598-21531-5" );
		assertValidISBN( "978-3-598-21532-2" );
		assertValidISBN( "978-3-598-21533-9" );
		assertValidISBN( "978-3-598-21534-6" );
		assertValidISBN( "978-3-598-21535-3" );
		assertValidISBN( "978-3-598-21536-0" );
		assertValidISBN( "978-3-598-21537-7" );
		assertValidISBN( "978-3-598-21538-4" );
		assertValidISBN( "978-3-598-21539-1" );
		assertValidISBN( "978-3-598-21540-7" );
		assertValidISBN( "978-3-598-21541-4" );
		assertValidISBN( "978-3-598-21542-1" );
		assertValidISBN( "978-3-598-21543-8" );
		assertValidISBN( "978-3-598-21544-5" );
		assertValidISBN( "978-3-598-21545-2" );
		assertValidISBN( "978-3-598-21546-9" );
		assertValidISBN( "978-3-598-21547-6" );
		assertValidISBN( "978-3-598-21548-3" );
		assertValidISBN( "978-3-598-21549-0" );
		assertValidISBN( "978-3-598-21550-6" );
		assertValidISBN( "978-3-598-21551-3" );
		assertValidISBN( "978-3-598-21552-0" );
		assertValidISBN( "978-3-598-21553-7" );
		assertValidISBN( "978-3-598-21554-4" );
		assertValidISBN( "978-3-598-21555-1" );
		assertValidISBN( "978-3-598-21556-8" );
		assertValidISBN( "978-3-598-21557-5" );
		assertValidISBN( "978-3-598-21558-2" );
		assertValidISBN( "978-3-598-21559-9" );
		assertValidISBN( "978-3-598-21560-5" );
		assertValidISBN( "978-3-598-21561-2" );
		assertValidISBN( "978-3-598-21562-9" );
		assertValidISBN( "978-3-598-21563-6" );
		assertValidISBN( "978-3-598-21564-3" );
		assertValidISBN( "978-3-598-21565-0" );
		assertValidISBN( "978-3-598-21566-7" );
		assertValidISBN( "978-3-598-21567-4" );
		assertValidISBN( "978-3-598-21568-1" );
		assertValidISBN( "978-3-598-21569-8" );
		assertValidISBN( "978-3-598-21570-4" );
		assertValidISBN( "978-3-598-21571-1" );
		assertValidISBN( "978-3-598-21572-8" );
		assertValidISBN( "978-3-598-21573-5" );
		assertValidISBN( "978-3-598-21574-2" );
		assertValidISBN( "978-3-598-21575-9" );
		assertValidISBN( "978-3-598-21576-6" );
		assertValidISBN( "978-3-598-21577-3" );
		assertValidISBN( "978-3-598-21578-0" );
		assertValidISBN( "978-3-598-21579-7" );
		assertValidISBN( "978-3-598-21580-3" );
		assertValidISBN( "978-3-598-21581-0" );
		assertValidISBN( "978-3-598-21582-7" );
		assertValidISBN( "978-3-598-21583-4" );
		assertValidISBN( "978-3-598-21584-1" );
		assertValidISBN( "978-3-598-21585-8" );
		assertValidISBN( "978-3-598-21586-5" );
		assertValidISBN( "978-3-598-21587-2" );
		assertValidISBN( "978-3-598-21588-9" );
		assertValidISBN( "978-3-598-21589-6" );
		assertValidISBN( "978-3-598-21590-2" );
		assertValidISBN( "978-3-598-21591-9" );
		assertValidISBN( "978-3-598-21592-6" );
		assertValidISBN( "978-3-598-21593-3" );
		assertValidISBN( "978-3-598-21594-0" );
		assertValidISBN( "978-3-598-21595-7" );
		assertValidISBN( "978-3-598-21596-4" );
		assertValidISBN( "978-3-598-21597-1" );
		assertValidISBN( "978-3-598-21598-8" );
		assertValidISBN( "978-3-598-21599-5" );
	}

	@Test
	public void invalidISBN13() throws Exception {
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN_13 ) );

		// invalid check-digit
		assertInvalidISBN( "978-123-456-789-6" );
		assertInvalidISBN( "978-91-983989-1-4" );
		assertInvalidISBN( "978-988-785-411-2" );
		assertInvalidISBN( "978-1-56619-909-1" );
		assertInvalidISBN( "978-1-4028-9462-0" );
		assertInvalidISBN( "978-0-85131-041-5" );
		assertInvalidISBN( "978-0-684-84328-1" );
		assertInvalidISBN( "978-1-84356-028-1" );
		assertInvalidISBN( "978-0-54560-495-4" );

		// invalid length
		assertInvalidISBN( "" );
		assertInvalidISBN( "978-0-54560-4" );
		assertInvalidISBN( "978-0-55555555555555" );
	}

	@Test
	@TestForIssue( jiraKey = "HV-1707")
	public void testValidISBNAny() {
		validator.initialize( initializeAnnotation( ISBN.Type.ANY ) );

		assertValidISBN( null );
		// ISBN10
		assertValidISBN( "99921-58-10-7" );
		assertValidISBN( "9971-5-0210-0" );
		assertValidISBN( "960-425-059-0" );
		assertValidISBN( "80-902734-1-6" );
		assertValidISBN( "0-9752298-0-X" );

		// ISBN13
		assertValidISBN( "978-123-456-789-7" );
		assertValidISBN( "978-91-983989-1-5" );
		assertValidISBN( "978-988-785-411-1" );
		assertValidISBN( "978-1-56619-909-4" );
		assertValidISBN( "978-1-4028-9462-6" );
		assertValidISBN( "978-0-85131-041-1" );

		// invalid cases
		assertInvalidISBN( "978-0-85131-041-0" );
		assertInvalidISBN( "80-902734-1-8" );
		assertInvalidISBN( "978-0-85131-0401-0" );
		assertInvalidISBN( "80-902734-10-8" );

		// incorrect length:
		assertInvalidISBN( "978-0-85" );
		assertInvalidISBN( "80-902734-1-877777" );
		assertInvalidISBN( "978-0-85131-0401-00074" );
		assertInvalidISBN( "80-902734-1" );

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
