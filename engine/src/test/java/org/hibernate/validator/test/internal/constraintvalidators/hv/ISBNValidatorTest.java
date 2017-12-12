/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.ISBN;
import org.hibernate.validator.internal.constraintvalidators.hv.ISBNValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

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
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN10 ) );

		assertValidISBN( null );
		assertValidISBN( "99921-58-10-7" );
		assertValidISBN( "9971-5-0210-0" );
		assertValidISBN( "960-425-059-0" );
		assertValidISBN( "80-902734-1-6" );
		assertValidISBN( "0-9752298-0-X" );
		assertValidISBN( "0-85131-041-9" );
		assertValidISBN( "0-684-84328-5" );
		assertValidISBN( "1-84356-028-3" );
	}

	@Test
	public void invalidISBN10() throws Exception {
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN10 ) );

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
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN13 ) );

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
	}

	@Test
	public void invalidISBN13() throws Exception {
		validator.initialize( initializeAnnotation( ISBN.Type.ISBN13 ) );

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
}
