package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.MaxLinesDef;
import org.hibernate.validator.constraints.MaxLines;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.Test;

/**
 * @since 9.2
 */
@TestForIssue(jiraKey = "HV-2293")
public class MaxLinesConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testMaxLinesValid() {
		Foo foo = new Foo( "single line" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testMaxLinesNullValid() {
		Foo foo = new Foo( null );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testMaxLinesTwoLinesValid() {
		Foo foo = new Foo( "line one\nline two" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testMaxLinesExceeded() {
		Foo foo = new Foo( "one\ntwo\nthree" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( MaxLines.class )
		);
	}

	@Test
	public void testProgrammaticDefinition() {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Bar.class )
				.field( "string" )
				.constraint( new MaxLinesDef().value( 2 ) );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Bar>> violations = programmaticValidator.validate( new Bar( "single line" ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Bar( null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Bar( "one\ntwo\nthree" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( MaxLines.class )
		);
	}

	// Intentionally unannotated — the constraint is applied programmatically in testProgrammaticDefinition()
	private static class Bar {

		private final String string;

		public Bar(String string) {
			this.string = string;
		}
	}

	private static class Foo {

		@MaxLines(2)
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
