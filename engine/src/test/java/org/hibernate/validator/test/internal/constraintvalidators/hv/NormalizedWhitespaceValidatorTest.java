package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.NormalizedWhitespaceDef;
import org.hibernate.validator.constraints.NormalizedWhitespace;
import org.hibernate.validator.constraints.NormalizedWhitespace.Rule;
import org.hibernate.validator.internal.constraintvalidators.hv.NormalizedWhitespaceValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link NormalizedWhitespace} constraint.
 *
 * @author Koen Aers
 */
public class NormalizedWhitespaceValidatorTest {

	private NormalizedWhitespaceValidator getValidator(Rule... rules) {
		ConstraintAnnotationDescriptor.Builder<NormalizedWhitespace> descriptorBuilder =
				new ConstraintAnnotationDescriptor.Builder<>( NormalizedWhitespace.class );
		if ( rules.length > 0 ) {
			descriptorBuilder.setAttribute( "value", rules );
		}
		NormalizedWhitespace annotation = descriptorBuilder.build().getAnnotation();
		NormalizedWhitespaceValidator validator = new NormalizedWhitespaceValidator();
		validator.initialize( annotation );
		return validator;
	}

	@Test
	public void testNullAndEmptyAreValid() {
		NormalizedWhitespaceValidator validator = getValidator();
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( "", null ) );
	}

	// --- Default rules: ONLY_SPACE + NO_CONSECUTIVE ---

	@Test
	public void testDefaultRulesAcceptSingleSpaces() {
		NormalizedWhitespaceValidator validator = getValidator();
		assertTrue( validator.isValid( "hello world", null ) );
		assertTrue( validator.isValid( "foo bar baz", null ) );
		assertTrue( validator.isValid( "a b c", null ) );
	}

	@Test
	public void testDefaultRulesAcceptNoWhitespace() {
		NormalizedWhitespaceValidator validator = getValidator();
		assertTrue( validator.isValid( "foobar", null ) );
	}

	@Test
	public void testDefaultRulesRejectConsecutiveSpaces() {
		NormalizedWhitespaceValidator validator = getValidator();
		assertFalse( validator.isValid( "hello  world", null ) );
		assertFalse( validator.isValid( "foo   bar", null ) );
	}

	@Test
	public void testDefaultRulesRejectTabs() {
		NormalizedWhitespaceValidator validator = getValidator();
		assertFalse( validator.isValid( "hello\tworld", null ) );
	}

	@Test
	public void testDefaultRulesRejectNewlines() {
		NormalizedWhitespaceValidator validator = getValidator();
		assertFalse( validator.isValid( "hello\nworld", null ) );
		assertFalse( validator.isValid( "hello\rworld", null ) );
	}

	@Test
	public void testDefaultRulesRejectFormFeed() {
		NormalizedWhitespaceValidator validator = getValidator();
		assertFalse( validator.isValid( "hello\fworld", null ) );
		assertFalse( validator.isValid( "hello\u000Bworld", null ) );
	}

	// --- NO_CONSECUTIVE only ---

	@Test
	public void testNoConsecutiveAcceptsSingleWhitespace() {
		NormalizedWhitespaceValidator validator = getValidator( Rule.NO_CONSECUTIVE );
		assertTrue( validator.isValid( "hello world", null ) );
		assertTrue( validator.isValid( "hello\tworld", null ) );
		assertTrue( validator.isValid( "hello\nworld", null ) );
	}

	@Test
	public void testNoConsecutiveRejectsConsecutiveWhitespace() {
		NormalizedWhitespaceValidator validator = getValidator( Rule.NO_CONSECUTIVE );
		assertFalse( validator.isValid( "hello  world", null ) );
		assertFalse( validator.isValid( "hello\t\tworld", null ) );
		assertFalse( validator.isValid( "hello \tworld", null ) );
		assertFalse( validator.isValid( "hello\n\nworld", null ) );
	}

	// --- NO_TABS only ---

	@Test
	public void testNoTabsAcceptsOtherWhitespace() {
		NormalizedWhitespaceValidator validator = getValidator( Rule.NO_TABS );
		assertTrue( validator.isValid( "hello world", null ) );
		assertTrue( validator.isValid( "hello  world", null ) );
		assertTrue( validator.isValid( "hello\nworld", null ) );
	}

	@Test
	public void testNoTabsRejectsTabs() {
		NormalizedWhitespaceValidator validator = getValidator( Rule.NO_TABS );
		assertFalse( validator.isValid( "hello\tworld", null ) );
	}

	// --- NO_FORM_FEED only ---

	@Test
	public void testNoFormFeedAcceptsOtherWhitespace() {
		NormalizedWhitespaceValidator validator = getValidator( Rule.NO_FORM_FEED );
		assertTrue( validator.isValid( "hello world", null ) );
		assertTrue( validator.isValid( "hello\tworld", null ) );
	}

	@Test
	public void testNoFormFeedRejectsFormFeedAndVerticalTab() {
		NormalizedWhitespaceValidator validator = getValidator( Rule.NO_FORM_FEED );
		assertFalse( validator.isValid( "hello\fworld", null ) );
		assertFalse( validator.isValid( "hello\u000Bworld", null ) );
	}

	// --- ONLY_SPACE only (without NO_CONSECUTIVE) ---

	@Test
	public void testOnlySpaceAcceptsSpaces() {
		NormalizedWhitespaceValidator validator = getValidator( Rule.ONLY_SPACE );
		assertTrue( validator.isValid( "hello world", null ) );
		assertTrue( validator.isValid( "hello  world", null ) );
	}

	@Test
	public void testOnlySpaceRejectsNonSpaceWhitespace() {
		NormalizedWhitespaceValidator validator = getValidator( Rule.ONLY_SPACE );
		assertFalse( validator.isValid( "hello\tworld", null ) );
		assertFalse( validator.isValid( "hello\nworld", null ) );
		assertFalse( validator.isValid( "hello\rworld", null ) );
		assertFalse( validator.isValid( "hello\fworld", null ) );
		assertFalse( validator.isValid( "hello\u000Bworld", null ) );
	}

	// --- Rule combinations ---

	@Test
	public void testNoTabsAndNoConsecutive() {
		NormalizedWhitespaceValidator validator = getValidator( Rule.NO_TABS, Rule.NO_CONSECUTIVE );
		assertTrue( validator.isValid( "hello world", null ) );
		assertTrue( validator.isValid( "hello\nworld", null ) );
		assertFalse( validator.isValid( "hello\tworld", null ) );
		assertFalse( validator.isValid( "hello  world", null ) );
	}

	// --- Edge cases ---

	@Test
	public void testSingleWhitespaceCharacter() {
		NormalizedWhitespaceValidator validator = getValidator();
		assertTrue( validator.isValid( " ", null ) );
	}

	@Test
	public void testWhitespaceAtStartAndEnd() {
		NormalizedWhitespaceValidator validator = getValidator();
		assertTrue( validator.isValid( " hello ", null ) );
	}

	@Test
	public void testCharSequence() {
		NormalizedWhitespaceValidator validator = getValidator();
		assertTrue( validator.isValid( new MyCustomStringImpl( "hello world" ), null ) );
		assertFalse( validator.isValid( new MyCustomStringImpl( "hello\tworld" ), null ) );
	}

	@Test
	public void testProgrammaticDefinition() {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( Foo.class )
				.field( "value" )
				.constraint( new NormalizedWhitespaceDef().value( Rule.ONLY_SPACE, Rule.NO_CONSECUTIVE ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		Set<ConstraintViolation<Foo>> constraintViolations =
				validator.validate( new Foo( "hello world" ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new Foo( "hello\tworld" ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NormalizedWhitespace.class )
		);
	}

	private static class Foo {

		private final String value;

		public Foo(String value) {
			this.value = value;
		}
	}
}
