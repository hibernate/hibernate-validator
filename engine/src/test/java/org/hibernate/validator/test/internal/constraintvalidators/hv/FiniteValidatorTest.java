package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.validator.constraints.Finite;
import org.hibernate.validator.internal.constraintvalidators.hv.FiniteValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.hv.FiniteValidatorForFloat;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link Finite} constraint.
 */
public class FiniteValidatorTest {

	private FiniteValidatorForDouble doubleValidator;
	private FiniteValidatorForFloat floatValidator;

	@BeforeEach
	public void setUp() {
		ConstraintAnnotationDescriptor.Builder<Finite> descriptorBuilder =
				new ConstraintAnnotationDescriptor.Builder<>( Finite.class );
		Finite annotation = descriptorBuilder.build().getAnnotation();

		doubleValidator = new FiniteValidatorForDouble();
		doubleValidator.initialize( annotation );

		floatValidator = new FiniteValidatorForFloat();
		floatValidator.initialize( annotation );
	}

	@Test
	public void testNullIsValid() {
		assertTrue( doubleValidator.isValid( null, null ) );
		assertTrue( floatValidator.isValid( null, null ) );
	}

	@Test
	public void testFiniteDoubleIsValid() {
		assertTrue( doubleValidator.isValid( 0.0, null ) );
		assertTrue( doubleValidator.isValid( 1.0, null ) );
		assertTrue( doubleValidator.isValid( -1.0, null ) );
		assertTrue( doubleValidator.isValid( Double.MAX_VALUE, null ) );
		assertTrue( doubleValidator.isValid( Double.MIN_VALUE, null ) );
		assertTrue( doubleValidator.isValid( -Double.MAX_VALUE, null ) );
	}

	@Test
	public void testDoubleNaNIsInvalid() {
		assertFalse( doubleValidator.isValid( Double.NaN, null ) );
	}

	@Test
	public void testDoublePositiveInfinityIsInvalid() {
		assertFalse( doubleValidator.isValid( Double.POSITIVE_INFINITY, null ) );
	}

	@Test
	public void testDoubleNegativeInfinityIsInvalid() {
		assertFalse( doubleValidator.isValid( Double.NEGATIVE_INFINITY, null ) );
	}

	@Test
	public void testFiniteFloatIsValid() {
		assertTrue( floatValidator.isValid( 0.0f, null ) );
		assertTrue( floatValidator.isValid( 1.0f, null ) );
		assertTrue( floatValidator.isValid( -1.0f, null ) );
		assertTrue( floatValidator.isValid( Float.MAX_VALUE, null ) );
		assertTrue( floatValidator.isValid( Float.MIN_VALUE, null ) );
		assertTrue( floatValidator.isValid( -Float.MAX_VALUE, null ) );
	}

	@Test
	public void testFloatNaNIsInvalid() {
		assertFalse( floatValidator.isValid( Float.NaN, null ) );
	}

	@Test
	public void testFloatPositiveInfinityIsInvalid() {
		assertFalse( floatValidator.isValid( Float.POSITIVE_INFINITY, null ) );
	}

	@Test
	public void testFloatNegativeInfinityIsInvalid() {
		assertFalse( floatValidator.isValid( Float.NEGATIVE_INFINITY, null ) );
	}
}
