package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.hibernate.validator.constraints.Even;
import org.hibernate.validator.constraints.Odd;
import org.hibernate.validator.internal.constraintvalidators.hv.EvenValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.hv.EvenValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.hv.EvenValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.hv.EvenValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.hv.EvenValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.hv.OddValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.hv.OddValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.hv.OddValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.hv.OddValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.hv.OddValidatorForShort;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link Even} and {@link Odd} constraints.
 *
 * @author Koen Aers
 */
public class EvenOddValidatorTest {

	// --- @Even ---

	@Test
	public void evenNullIsValid() {
		EvenValidatorForInteger validator = getEvenValidatorForInteger();
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void evenZeroIsValid() {
		EvenValidatorForInteger validator = getEvenValidatorForInteger();
		assertTrue( validator.isValid( 0, null ) );
	}

	@Test
	public void evenIntegerValues() {
		EvenValidatorForInteger validator = getEvenValidatorForInteger();
		assertTrue( validator.isValid( 2, null ) );
		assertTrue( validator.isValid( -4, null ) );
		assertTrue( validator.isValid( 100, null ) );
		assertFalse( validator.isValid( 1, null ) );
		assertFalse( validator.isValid( -3, null ) );
		assertFalse( validator.isValid( 99, null ) );
	}

	@Test
	public void evenLongValues() {
		EvenValidatorForLong validator = getEvenValidatorForLong();
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( 42L, null ) );
		assertFalse( validator.isValid( 43L, null ) );
	}

	@Test
	public void evenByteValues() {
		EvenValidatorForByte validator = getEvenValidatorForByte();
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( (byte) 4, null ) );
		assertFalse( validator.isValid( (byte) 5, null ) );
	}

	@Test
	public void evenShortValues() {
		EvenValidatorForShort validator = getEvenValidatorForShort();
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( (short) 6, null ) );
		assertFalse( validator.isValid( (short) 7, null ) );
	}

	@Test
	public void evenBigIntegerValues() {
		EvenValidatorForBigInteger validator = getEvenValidatorForBigInteger();
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( BigInteger.valueOf( 1000 ), null ) );
		assertFalse( validator.isValid( BigInteger.valueOf( 1001 ), null ) );
	}

	// --- @Odd ---

	@Test
	public void oddNullIsValid() {
		OddValidatorForInteger validator = getOddValidatorForInteger();
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void oddZeroIsInvalid() {
		OddValidatorForInteger validator = getOddValidatorForInteger();
		assertFalse( validator.isValid( 0, null ) );
	}

	@Test
	public void oddIntegerValues() {
		OddValidatorForInteger validator = getOddValidatorForInteger();
		assertTrue( validator.isValid( 1, null ) );
		assertTrue( validator.isValid( -3, null ) );
		assertTrue( validator.isValid( 99, null ) );
		assertFalse( validator.isValid( 2, null ) );
		assertFalse( validator.isValid( -4, null ) );
		assertFalse( validator.isValid( 100, null ) );
	}

	@Test
	public void oddLongValues() {
		OddValidatorForLong validator = getOddValidatorForLong();
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( 43L, null ) );
		assertFalse( validator.isValid( 42L, null ) );
	}

	@Test
	public void oddByteValues() {
		OddValidatorForByte validator = getOddValidatorForByte();
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( (byte) 5, null ) );
		assertFalse( validator.isValid( (byte) 4, null ) );
	}

	@Test
	public void oddShortValues() {
		OddValidatorForShort validator = getOddValidatorForShort();
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( (short) 7, null ) );
		assertFalse( validator.isValid( (short) 6, null ) );
	}

	@Test
	public void oddBigIntegerValues() {
		OddValidatorForBigInteger validator = getOddValidatorForBigInteger();
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( BigInteger.valueOf( 1001 ), null ) );
		assertFalse( validator.isValid( BigInteger.valueOf( 1000 ), null ) );
	}

	// --- Even validator factory methods ---

	private EvenValidatorForInteger getEvenValidatorForInteger() {
		EvenValidatorForInteger validator = new EvenValidatorForInteger();
		validator.initialize( new ConstraintAnnotationDescriptor.Builder<>( Even.class ).build().getAnnotation() );
		return validator;
	}

	private EvenValidatorForLong getEvenValidatorForLong() {
		EvenValidatorForLong validator = new EvenValidatorForLong();
		validator.initialize( new ConstraintAnnotationDescriptor.Builder<>( Even.class ).build().getAnnotation() );
		return validator;
	}

	private EvenValidatorForByte getEvenValidatorForByte() {
		EvenValidatorForByte validator = new EvenValidatorForByte();
		validator.initialize( new ConstraintAnnotationDescriptor.Builder<>( Even.class ).build().getAnnotation() );
		return validator;
	}

	private EvenValidatorForShort getEvenValidatorForShort() {
		EvenValidatorForShort validator = new EvenValidatorForShort();
		validator.initialize( new ConstraintAnnotationDescriptor.Builder<>( Even.class ).build().getAnnotation() );
		return validator;
	}

	private EvenValidatorForBigInteger getEvenValidatorForBigInteger() {
		EvenValidatorForBigInteger validator = new EvenValidatorForBigInteger();
		validator.initialize( new ConstraintAnnotationDescriptor.Builder<>( Even.class ).build().getAnnotation() );
		return validator;
	}

	// --- Odd validator factory methods ---

	private OddValidatorForInteger getOddValidatorForInteger() {
		OddValidatorForInteger validator = new OddValidatorForInteger();
		validator.initialize( new ConstraintAnnotationDescriptor.Builder<>( Odd.class ).build().getAnnotation() );
		return validator;
	}

	private OddValidatorForLong getOddValidatorForLong() {
		OddValidatorForLong validator = new OddValidatorForLong();
		validator.initialize( new ConstraintAnnotationDescriptor.Builder<>( Odd.class ).build().getAnnotation() );
		return validator;
	}

	private OddValidatorForByte getOddValidatorForByte() {
		OddValidatorForByte validator = new OddValidatorForByte();
		validator.initialize( new ConstraintAnnotationDescriptor.Builder<>( Odd.class ).build().getAnnotation() );
		return validator;
	}

	private OddValidatorForShort getOddValidatorForShort() {
		OddValidatorForShort validator = new OddValidatorForShort();
		validator.initialize( new ConstraintAnnotationDescriptor.Builder<>( Odd.class ).build().getAnnotation() );
		return validator;
	}

	private OddValidatorForBigInteger getOddValidatorForBigInteger() {
		OddValidatorForBigInteger validator = new OddValidatorForBigInteger();
		validator.initialize( new ConstraintAnnotationDescriptor.Builder<>( Odd.class ).build().getAnnotation() );
		return validator;
	}

}
