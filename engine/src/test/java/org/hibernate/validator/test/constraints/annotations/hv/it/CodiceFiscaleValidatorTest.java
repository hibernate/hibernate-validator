package org.hibernate.validator.test.constraints.annotations.hv.it;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import org.hibernate.validator.constraints.it.CodiceFiscale;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.junit.jupiter.api.Test;

public class CodiceFiscaleValidatorTest extends AbstractConstrainedTest {

	@Test
	public void valid_natural_person_code_validates() {
		assertNoViolations( validator.validate( new Taxpayer( "RSSMRA80A01H501U" ) ) );
		assertNoViolations( validator.validate( new Taxpayer( "MRTMTT25D09F205Z" ) ) );
		assertNoViolations( validator.validate( new Taxpayer( "rssmra80a01h501u" ) ) );
	}

	@Test
	public void day_of_birth_of_a_woman_validates() {
		// 40 is added to the day: the 1st becomes 41, the 31st becomes 71
		assertNoViolations( validator.validate( new Taxpayer( "RSSMRA80A41H501Y" ) ) );
		assertNoViolations( validator.validate( new Taxpayer( "RSSMRA80A71H501B" ) ) );
	}

	@Test
	public void code_with_digits_replaced_by_letters_validates() {
		// "omocodia": the digits are replaced starting from the rightmost one, and the check character changes with them
		assertNoViolations( validator.validate( new Taxpayer( "RSSMRA80A01H50MM" ) ) );
		assertNoViolations( validator.validate( new Taxpayer( "RSSMRA80A01H5LMX" ) ) );
		assertNoViolations( validator.validate( new Taxpayer( "RSSMRAULALMHRLMD" ) ) );
	}

	@Test
	public void valid_legal_entity_code_validates() {
		assertNoViolations( validator.validate( new Taxpayer( "00743110157" ) ) );
	}

	@Test
	public void null_code_validates() {
		assertNoViolations( validator.validate( new Taxpayer( null ) ) );
	}

	@Test
	public void invalid_code_creates_constraint_violation() {
		// wrong check character
		assertInvalid( "RSSMRA80A01H501V" );
		assertInvalid( "00743110158" );
		// days 00, 32 and 72 do not exist, even with a matching check character
		assertInvalid( "RSSMRA80A00H501V" );
		assertInvalid( "RSSMRA80A32H501C" );
		assertInvalid( "RSSMRA80A72H501G" );
		// F is not a month letter and O does not replace any digit, even with a matching check character
		assertInvalid( "RSSMRA80F01H501G" );
		assertInvalid( "RSSMRA80A01H50OF" );
		// wrong length or separators
		assertInvalid( "RSSMRA80A01H501" );
		assertInvalid( "RSSMRA80A01H501UU" );
		assertInvalid( "RSS MRA 80A01 H501U" );
		assertInvalid( "0074311015" );
		assertInvalid( "00000000000" );
		assertInvalid( "" );
	}

	private void assertInvalid(String codiceFiscale) {
		assertThat( validator.validate( new Taxpayer( codiceFiscale ) ) )
				.containsOnlyViolations(
						violationOf( CodiceFiscale.class ).withProperty( "codiceFiscale" )
				);
	}

	public static class Taxpayer {

		@CodiceFiscale
		private String codiceFiscale;

		public Taxpayer(String codiceFiscale) {
			this.codiceFiscale = codiceFiscale;
		}
	}
}
