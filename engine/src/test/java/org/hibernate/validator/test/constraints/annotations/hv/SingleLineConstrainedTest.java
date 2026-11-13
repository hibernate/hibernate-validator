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
import org.hibernate.validator.cfg.defs.SingleLineDef;
import org.hibernate.validator.constraints.SingleLine;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.Test;

/**
 * @since 9.2
 */
@TestForIssue(jiraKey = "HV-2292")
public class SingleLineConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testSingleLineValid() {
		Foo foo = new Foo( "foobar" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testSingleLineNullValid() {
		Foo foo = new Foo( null );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testSingleLineWithLineFeedInvalid() {
		Foo foo = new Foo( "foo\nbar" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( SingleLine.class )
		);
	}

	@Test
	public void testSingleLineWithCarriageReturnInvalid() {
		Foo foo = new Foo( "foo\rbar" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( SingleLine.class )
		);
	}

	@Test
	public void testProgrammaticDefinition() {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Bar.class )
				.field( "string" )
				.constraint( new SingleLineDef() );
		config.addMapping( mapping );
		Validator programmaticValidator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Bar>> violations = programmaticValidator.validate( new Bar( "foobar" ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Bar( null ) );
		assertNoViolations( violations );

		violations = programmaticValidator.validate( new Bar( "foo\nbar" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( SingleLine.class )
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

		@SingleLine
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}
}
