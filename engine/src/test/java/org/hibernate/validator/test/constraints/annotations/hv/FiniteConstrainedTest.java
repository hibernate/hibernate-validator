package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.FiniteDef;
import org.hibernate.validator.constraints.Finite;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.Test;

/**
 * @author Koen Aers
 */
@TestForIssue(jiraKey = "HV-2297")
public class FiniteConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void nullDoubleIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( null, null ) );
		assertNoViolations( violations );
	}

	@Test
	public void finiteValuesAreValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 1.0, 1.0f ) );
		assertNoViolations( violations );
	}

	@Test
	public void doubleNaNIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( Double.NaN, 1.0f ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Finite.class )
		);
	}

	@Test
	public void doubleInfinityIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( Double.POSITIVE_INFINITY, 1.0f ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Finite.class )
		);
	}

	@Test
	public void floatNaNIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 1.0, Float.NaN ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Finite.class )
		);
	}

	@Test
	public void floatInfinityIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( 1.0, Float.NEGATIVE_INFINITY ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Finite.class )
		);
	}

	@Test
	public void testProgrammaticDefinition() {
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration();
		ConstraintMapping mapping = configuration.createConstraintMapping();
		mapping.type( Bar.class )
				.field( "doubleValue" )
				.constraint( new FiniteDef() )
				.field( "floatValue" )
				.constraint( new FiniteDef() );
		configuration.addMapping( mapping );
		Validator programmaticValidator = configuration.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Bar>> violations =
				programmaticValidator.validate( new Bar( Double.NaN, Float.POSITIVE_INFINITY ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Finite.class ),
				violationOf( Finite.class )
		);
	}

	private static class Foo {

		@Finite
		private final Double doubleValue;

		@Finite
		private final Float floatValue;

		public Foo(Double doubleValue, Float floatValue) {
			this.doubleValue = doubleValue;
			this.floatValue = floatValue;
		}
	}

	// Intentionally not annotated — used for programmatic definition test
	private static class Bar {

		private final Double doubleValue;
		private final Float floatValue;

		public Bar(Double doubleValue, Float floatValue) {
			this.doubleValue = doubleValue;
			this.floatValue = floatValue;
		}
	}
}
