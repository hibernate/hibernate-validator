package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.validator.constraints.SingleLine;
import org.hibernate.validator.internal.constraintvalidators.hv.SingleLineValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link SingleLine} constraint.
 */
public class SingleLineValidatorTest {

	private SingleLineValidator constraint;

	@BeforeEach
	public void setUp() {
		ConstraintAnnotationDescriptor.Builder<SingleLine> descriptorBuilder =
				new ConstraintAnnotationDescriptor.Builder<>( SingleLine.class );
		SingleLine annotation = descriptorBuilder.build().getAnnotation();
		constraint = new SingleLineValidator();
		constraint.initialize( annotation );
	}

	@Test
	public void testNullAndEmptyAreValid() {
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "", null ) );
	}

	@Test
	public void testSingleLineStringsAreValid() {
		assertTrue( constraint.isValid( "hello world", null ) );
		assertTrue( constraint.isValid( "a", null ) );
		assertTrue( constraint.isValid( "foo\tbar", null ) );
		assertTrue( constraint.isValid( "hello world!", null ) );
	}

	@Test
	public void testLineFeedIsInvalid() {
		assertFalse( constraint.isValid( "hello\nworld", null ) );
		assertFalse( constraint.isValid( "\n", null ) );
		assertFalse( constraint.isValid( "trailing\n", null ) );
		assertFalse( constraint.isValid( "\nleading", null ) );
	}

	@Test
	public void testCarriageReturnIsInvalid() {
		assertFalse( constraint.isValid( "hello\rworld", null ) );
		assertFalse( constraint.isValid( "\r", null ) );
	}

	@Test
	public void testWindowsNewlineIsInvalid() {
		assertFalse( constraint.isValid( "hello\r\nworld", null ) );
	}

	@Test
	public void testNextLineIsInvalid() {
		assertFalse( constraint.isValid( "hello\u0085world", null ) );
	}

	@Test
	public void testLineSeparatorIsInvalid() {
		assertFalse( constraint.isValid( "hello\u2028world", null ) );
	}

	@Test
	public void testParagraphSeparatorIsInvalid() {
		assertFalse( constraint.isValid( "hello\u2029world", null ) );
	}

	@Test
	public void testCharSequence() {
		assertTrue( constraint.isValid( new MyCustomStringImpl( "hello world" ), null ) );
		assertFalse( constraint.isValid( new MyCustomStringImpl( "hello\nworld" ), null ) );
	}
}
