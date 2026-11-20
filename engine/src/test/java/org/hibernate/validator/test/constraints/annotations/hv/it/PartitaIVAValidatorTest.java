package org.hibernate.validator.test.constraints.annotations.hv.it;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import org.hibernate.validator.constraints.it.PartitaIVA;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

@TestForIssue(jiraKey = "HV-2295")
public class PartitaIVAValidatorTest extends AbstractConstrainedTest {

	@Test
	public void valid_number_validates() {
		assertNoViolations( validator.validate( new Company( "00743110157" ) ) );
		assertNoViolations( validator.validate( new Company( "07643520567" ) ) );
	}

	@Test
	public void null_number_validates() {
		assertNoViolations( validator.validate( new Company( null ) ) );
	}

	@Test
	public void invalid_number_creates_constraint_violation() {
		// wrong check digit
		assertInvalid( "00743110158" );
		// all zeros satisfy the checksum but are not a number
		assertInvalid( "00000000000" );
		// wrong length, country prefix, letters
		assertInvalid( "0074311015" );
		assertInvalid( "007431101577" );
		assertInvalid( "IT00743110157" );
		assertInvalid( "0074311015A" );
		assertInvalid( "" );
	}

	private void assertInvalid(String partitaIva) {
		assertThat( validator.validate( new Company( partitaIva ) ) )
				.containsOnlyViolations(
						violationOf( PartitaIVA.class ).withProperty( "partitaIva" )
				);
	}

	public static class Company {

		@PartitaIVA
		private String partitaIva;

		public Company(String partitaIva) {
			this.partitaIva = partitaIva;
		}
	}
}
