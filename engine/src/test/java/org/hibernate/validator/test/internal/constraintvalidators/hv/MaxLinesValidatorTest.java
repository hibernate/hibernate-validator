package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.validator.constraints.MaxLines;
import org.hibernate.validator.internal.constraintvalidators.hv.MaxLinesValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;

import org.junit.jupiter.api.Test;

/**
 * Tests the {@link MaxLines} constraint.
 */
public class MaxLinesValidatorTest {

	private MaxLinesValidator createValidator(int value) {
		ConstraintAnnotationDescriptor.Builder<MaxLines> descriptorBuilder =
				new ConstraintAnnotationDescriptor.Builder<>( MaxLines.class );
		descriptorBuilder.setAttribute( "value", value );
		MaxLines annotation = descriptorBuilder.build().getAnnotation();
		MaxLinesValidator validator = new MaxLinesValidator();
		validator.initialize( annotation );
		return validator;
	}

	@Test
	public void testNullIsValid() {
		MaxLinesValidator validator = createValidator( 1 );
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void testEmptyIsValid() {
		MaxLinesValidator validator = createValidator( 1 );
		assertTrue( validator.isValid( "", null ) );
	}

	@Test
	public void testSingleLineWithMaxOne() {
		MaxLinesValidator validator = createValidator( 1 );
		assertTrue( validator.isValid( "hello world", null ) );
	}

	@Test
	public void testTwoLinesWithMaxOne() {
		MaxLinesValidator validator = createValidator( 1 );
		assertFalse( validator.isValid( "hello\nworld", null ) );
	}

	@Test
	public void testTwoLinesWithMaxTwo() {
		MaxLinesValidator validator = createValidator( 2 );
		assertTrue( validator.isValid( "hello\nworld", null ) );
	}

	@Test
	public void testMultipleLines() {
		MaxLinesValidator validator = createValidator( 3 );
		assertTrue( validator.isValid( "a\nb\nc", null ) );
		assertFalse( validator.isValid( "a\nb\nc\nd", null ) );
	}

	@Test
	public void testCarriageReturn() {
		MaxLinesValidator validator = createValidator( 2 );
		assertTrue( validator.isValid( "hello\rworld", null ) );
		assertFalse( validator.isValid( "a\rb\rc", null ) );
	}

	@Test
	public void testWindowsNewline() {
		MaxLinesValidator validator = createValidator( 2 );
		assertTrue( validator.isValid( "hello\r\nworld", null ) );
		assertFalse( validator.isValid( "a\r\nb\r\nc", null ) );
	}

	@Test
	public void testCrlfCountsAsOneLine() {
		MaxLinesValidator validator = createValidator( 2 );
		assertTrue( validator.isValid( "hello\r\nworld", null ) );
	}

	@Test
	public void testNextLine() {
		MaxLinesValidator validator = createValidator( 1 );
		assertFalse( validator.isValid( "hello\u0085world", null ) );
	}

	@Test
	public void testLineSeparator() {
		MaxLinesValidator validator = createValidator( 1 );
		assertFalse( validator.isValid( "hello\u2028world", null ) );
	}

	@Test
	public void testParagraphSeparator() {
		MaxLinesValidator validator = createValidator( 1 );
		assertFalse( validator.isValid( "hello\u2029world", null ) );
	}

	@Test
	public void testTrailingNewline() {
		MaxLinesValidator validator = createValidator( 2 );
		assertTrue( validator.isValid( "hello\n", null ) );
	}

	@Test
	public void testTrailingNewlineExceedsMax() {
		MaxLinesValidator validator = createValidator( 1 );
		assertFalse( validator.isValid( "hello\n", null ) );
	}

	@Test
	public void testCharSequence() {
		MaxLinesValidator validator = createValidator( 1 );
		assertTrue( validator.isValid( new MyCustomStringImpl( "hello world" ), null ) );
		assertFalse( validator.isValid( new MyCustomStringImpl( "hello\nworld" ), null ) );
	}

	@Test
	public void testInvalidMaxValue() {
		assertThrows( Exception.class, () -> createValidator( 0 ) );
		assertThrows( Exception.class, () -> createValidator( -1 ) );
	}
}
