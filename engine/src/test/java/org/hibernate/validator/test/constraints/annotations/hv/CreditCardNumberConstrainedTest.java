/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Random;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.CreditCardNumber;
import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.internal.constraintvalidators.hv.LuhnCheckValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class CreditCardNumberConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testCreditCardNumber() {
		Foo foo = new Foo( "5412 3456 7890 125", "5412 3456 7890 125" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testCreditCardNumberInvalid() {
		Foo foo = new Foo( "5412 3456 7890", "5412 3456 7890" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( CreditCardNumber.class ), violationOf( CreditCardNumber.class )
		);
	}

	@Test
	public void testStandaloneConstraint() {
		assertTrue( validator.getConstraintsForClass( Foo.class )
				.getConstraintsForProperty( "number" ).getConstraintDescriptors().iterator().next()
				.getComposingConstraints().isEmpty() );
	}

	@Test(dataProvider = "creditCardNumbers")
	public void testCreditCardNumberBoundaries(CharSequence number, boolean strictValid, boolean ignoringValid) {
		assertEquals( validator.validateValue( CreditCardNumbers.class, "strict", number ).isEmpty(), strictValid );
		assertEquals( validator.validateValue( CreditCardNumbers.class, "ignoring", number ).isEmpty(), ignoringValid );
	}

	@DataProvider
	public Object[][] creditCardNumbers() {
		return new Object[][] {
				{ null, true, true },
				{ "", false, false },
				{ "0", true, true },
				{ "1", false, false },
				{ "00", true, true },
				{ "18", true, true },
				{ "٠", true, true },
				{ "０", true, true },
				{ "abc0", false, true },
				{ " -0", false, true },
				{ "𝟘", false, false },
				{ "𝟘0", false, true },
				{ "0 ", false, false },
				{ "-", false, false },
				{ "79927398713", true, true },
				{ "7992-7398 713", false, true },
				{ "79927398713-", false, false },
				{ "79927398713\n", false, false },
				{ "٧٩٩٢٧٣٩٨٧١٣", true, false },
				{ "٧٩٩٢٧٣٩٨٧١٠", false, true },
				{ "7992739871٣", true, true },
				{ "1٢5", true, false },
				{ "1٢8", false, true },
				{ new StringBuilder( "79927398713" ), true, true },
				{ new StringBuilder( "7992-7398-713" ), false, true },
				{ "0".repeat( 100_000 ), true, true }
		};
	}

	@Test
	public void testMatchesLegacyLuhnValidator() {
		LuhnCheckValidator strict = legacyValidator( false );
		LuhnCheckValidator ignoring = legacyValidator( true );
		Random random = new Random( 906 );
		String mixedCharacters = "0123456789٠١٢٣٤٥٦٧٨٩０１２３４５６７８９ -a";
		for ( int sample = 0; sample < 500; sample++ ) {
			StringBuilder number = new StringBuilder();
			int length = random.nextInt( 40 );
			for ( int i = 0; i < length; i++ ) {
				number.append( sample % 3 == 0
						? (char) ( '0' + random.nextInt( 10 ) )
						: mixedCharacters.charAt( random.nextInt( mixedCharacters.length() ) ) );
			}
			assertEquals( validator.validateValue( CreditCardNumbers.class, "strict", number ).isEmpty(),
					strict.isValid( number, null ), number.toString() );
			assertEquals( validator.validateValue( CreditCardNumbers.class, "ignoring", number ).isEmpty(),
					ignoring.isValid( number, null ), number.toString() );
		}
	}

	private static LuhnCheckValidator legacyValidator(boolean ignoreNonDigitCharacters) {
		ConstraintAnnotationDescriptor.Builder<LuhnCheck> descriptor = new ConstraintAnnotationDescriptor.Builder<>( LuhnCheck.class );
		descriptor.setAttribute( "ignoreNonDigitCharacters", ignoreNonDigitCharacters );
		LuhnCheckValidator validator = new LuhnCheckValidator();
		validator.initialize( descriptor.build().getAnnotation() );
		return validator;
	}

	private static class CreditCardNumbers {
		@CreditCardNumber
		private CharSequence strict;

		@CreditCardNumber(ignoreNonDigitCharacters = true)
		private CharSequence ignoring;
	}

	private static class Foo {

		@CreditCardNumber(ignoreNonDigitCharacters = true)
		private final String number;
		@CreditCardNumber(ignoreNonDigitCharacters = true)
		private final CharSequence anotherNumber;

		public Foo(String number, CharSequence anotherNumber) {
			this.number = number;
			this.anotherNumber = anotherNumber;
		}
	}
}
