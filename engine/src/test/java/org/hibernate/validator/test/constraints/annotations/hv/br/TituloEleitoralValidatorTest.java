/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv.br;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.Mod11Check;
import org.hibernate.validator.constraints.br.TituloEleitoral;
import org.hibernate.validator.internal.constraintvalidators.hv.Mod11CheckValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TituloEleitoralValidatorTest extends AbstractConstrainedTest {
	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testCorrectFormattedTituloEleitoral() {
		assertNoViolations( validator.validate( new Person( "040806680957" ) ) );
		assertNoViolations( validator.validate( new Person( "038763000914" ) ) );
		assertNoViolations( validator.validate( new Person( "975993331007" ) ) );
		assertNoViolations( validator.validate( new Person( "524384240701" ) ) );
		assertNoViolations( validator.validate( new Person( "311533282500" ) ) );
		assertNoViolations( validator.validate( new Person( "083578481406" ) ) );
		assertNoViolations( validator.validate( new Person( "233838490205" ) ) );
		assertNoViolations( validator.validate( new Person( "585052440116" ) ) );
		assertNoViolations( validator.validate( new Person( "650648840264" ) ) );
		assertNoViolations( validator.validate( new Person( "357866370213" ) ) );
		assertNoViolations( validator.validate( new Person( "074447240264" ) ) );
		assertNoViolations( validator.validate( new Person( "164284280213" ) ) );
		assertNoViolations( validator.validate( new Person( "465667341619" ) ) );
		assertNoViolations( validator.validate( new Person( "362133720779" ) ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-491")
	public void testIncorrectFormattedTituloEleitoral() {
		Set<ConstraintViolation<Person>> violations = validator.validate( new Person( "48255-77" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( TituloEleitoral.class ).withProperty( "tituloEleitor" )
		);
	}

	@Test
	public void testStandaloneConstraint() {
		assertTrue( validator.getConstraintsForClass( Person.class )
				.getConstraintsForProperty( "tituloEleitor" ).getConstraintDescriptors().iterator().next()
				.getComposingConstraints().isEmpty() );
	}

	@Test(dataProvider = "tituloEleitoralBoundaries")
	public void testTituloEleitoralBoundaries(CharSequence tituloEleitoral, boolean valid) {
		assertEquals( validator.validate( new Person( tituloEleitoral ) ).isEmpty(), valid );
	}

	@DataProvider
	public Object[][] tituloEleitoralBoundaries() {
		return new Object[][] {
				{ null, true },
				{ "", false },
				{ "000000000000", true },
				// First-checksum remainders zero and one both produce zero.
				{ "000000140000", true },
				{ "000000060000", true },
				// The second checksum has remainder one, which also produces zero.
				{ "000000000400", true },
				{ "040806680967", false },
				{ "040806680958", false },
				{ "00000000000٠", false },
				{ "０00000000000", false },
				{ "0000000000-0", false },
				{ "00000000000 ", false },
				{ " 00000000000", false },
				{ "00000000000\n", false },
				{ "00000000000", false },
				{ "0000000000000", false },
				{ new StringBuilder( "040806680957" ), true },
				{ "0".repeat( 100_000 ), false }
		};
	}

	@Test
	public void testMatchesLegacyMod11Validators() {
		Mod11CheckValidator first = legacyValidator( 0, 7, 10 );
		Mod11CheckValidator second = legacyValidator( 8, 10, 11 );
		Pattern legacyFormat = Pattern.compile( "[0-9]{12}" );
		Random random = new Random( 491 );
		String mixedCharacters = "0123456789٠١٢٣٤٥٦٧٨٩０１２３４５６７８９ -a";
		for ( int sample = 0; sample < 600; sample++ ) {
			StringBuilder number = new StringBuilder();
			for ( int i = 0; i < 12; i++ ) {
				number.append( sample % 2 == 0
						? (char) ( '0' + random.nextInt( 10 ) )
						: mixedCharacters.charAt( random.nextInt( mixedCharacters.length() ) ) );
			}
			assertEquals( validator.validate( new Person( number ) ).isEmpty(),
					legacyFormat.matcher( number ).matches() && first.isValid( number, null ) && second.isValid( number, null ),
					number.toString() );
		}
	}

	private static Mod11CheckValidator legacyValidator(int startIndex, int endIndex, int checkDigitIndex) {
		ConstraintAnnotationDescriptor.Builder<Mod11Check> descriptor = new ConstraintAnnotationDescriptor.Builder<>( Mod11Check.class );
		descriptor.setAttribute( "threshold", 9 );
		descriptor.setAttribute( "treatCheck10As", '0' );
		descriptor.setAttribute( "startIndex", startIndex );
		descriptor.setAttribute( "endIndex", endIndex );
		descriptor.setAttribute( "checkDigitIndex", checkDigitIndex );
		Mod11CheckValidator validator = new Mod11CheckValidator();
		validator.initialize( descriptor.build().getAnnotation() );
		return validator;
	}

	public static class Person {
		@TituloEleitoral
		private CharSequence tituloEleitor;

		public Person(CharSequence tituloEleitor) {
			this.tituloEleitor = tituloEleitor;
		}

		public CharSequence getTituloEleitor() {
			return tituloEleitor;
		}
	}
}
