/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.ru;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.constraints.ru.INN;
import org.hibernate.validator.internal.constraintvalidators.hv.ru.INNValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A set of tests for {@link INN} constraint validator ({@link INNValidator}),
 * which make sure that validation is performed correctly.
 *
 * @author Artem Boiarshinov
 * @see <a href="http://mellarius.ru/random-inn">fake INN generator</a>
 */
public class INNValidatorTest {

	private INNValidator validator;

	@BeforeMethod
	public void setUp() {
		validator = new INNValidator();
	}

	@Test
	public void validIndividualTypeINN() {
		validator.initialize( initializeAnnotation( INN.Type.INDIVIDUAL ) );

		assertValidINN( null );
		assertValidINN( "246964567008" );
		assertValidINN( "356393289962" );
		assertValidINN( "279837166431" );
		assertValidINN( "827175083460" );
		assertValidINN( "789429596404" );
		assertValidINN( "929603416330" );
		assertValidINN( "086229647992" );
	}

	@Test
	public void invalidIndividualTypeINN() {
		validator.initialize( initializeAnnotation( INN.Type.INDIVIDUAL ) );

		//invalid checksum
		assertInvalidINN( "012345678912" );
		assertInvalidINN( "246964567009" );

		//invalid symbols
		assertInvalidINN( "a46964567008" );

		//invalid length
		assertInvalidINN( "" );
		assertInvalidINN( "90660563173" );
		assertInvalidINN( "9066056317378" );

		//invalid type
		assertInvalidINN( "4546366155" );
	}

	@Test
	public void validJuridicalTypeINN() {
		validator.initialize( initializeAnnotation( INN.Type.JURIDICAL ) );

		assertValidINN( null );
		assertValidINN( "0305773929" );
		assertValidINN( "5496344268" );
		assertValidINN( "0314580754" );
		assertValidINN( "8652697156" );
		assertValidINN( "3527694367" );
		assertValidINN( "8771236130" );
		assertValidINN( "9254906927" );
	}

	@Test
	public void invalidJuridicalTypeINN() {
		validator.initialize( initializeAnnotation( INN.Type.JURIDICAL ) );

		//invalid checksum
		assertInvalidINN( "0123456789" );
		assertInvalidINN( "0305773928" );

		//invalid symbols
		assertInvalidINN( "a305773929" );

		//invalid length
		assertInvalidINN( "" );
		assertInvalidINN( "906605631" );
		assertInvalidINN( "90660563173" );

		//invalid type
		assertInvalidINN( "246964567008" );
	}

	@Test
	public void testAnyTypeINN() {
		validator.initialize( initializeAnnotation( INN.Type.ANY ) );

		final String personalValidINN = "246964567008";
		final String juridicalValidINN = "5496344268";
		final String personalInvalidINN = "246964567009";
		final String juridicalInvalidINN = "0305773928";

		assertValidINN( null );
		assertValidINN( personalValidINN );
		assertValidINN( juridicalValidINN );
		assertInvalidINN( personalInvalidINN );
		assertInvalidINN( juridicalInvalidINN );
	}

	private void assertValidINN(String inn) {
		assertTrue( validator.isValid( inn, null ), inn + " should be a valid INN" );
	}

	private void assertInvalidINN(String inn) {
		assertFalse( validator.isValid( inn, null ), inn + " should be a invalid INN" );
	}

	private INN initializeAnnotation(INN.Type type) {
		ConstraintAnnotationDescriptor.Builder<INN> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( INN.class );
		descriptorBuilder.setAttribute( "type", type );
		return descriptorBuilder.build().getAnnotation();
	}
}
