/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeOrZeroValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.NegativeValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveOrZeroValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.sign.PositiveValidatorForCharSequence;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.testng.annotations.Test;

import jakarta.validation.constraints.Negative;
import jakarta.validation.constraints.NegativeOrZero;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * @author Guillaume Smet
 */
public class NegativePositiveValidatorForStringTest {

	@Test
	public void testIsValidPositiveValidator() {
		ConstraintAnnotationDescriptor.Builder<Positive> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Positive.class );
		descriptorBuilder.setMessage( "{validator.positive}" );
		Positive m = descriptorBuilder.build().getAnnotation();

		PositiveValidatorForCharSequence constraint = new PositiveValidatorForCharSequence();
		constraint.initialize( m );

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "15", null ) );
		assertTrue( constraint.isValid( "15.0", null ) );
		assertFalse( constraint.isValid( "0", null ) );
		assertFalse( constraint.isValid( "-10", null ) );
		assertFalse( constraint.isValid( "-14.99", null ) );

		// number format exception
		assertFalse( constraint.isValid( "15l", null ) );
	}

	@Test
	public void testIsValidPositiveOrZeroValidator() {
		ConstraintAnnotationDescriptor.Builder<PositiveOrZero> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( PositiveOrZero.class );
		descriptorBuilder.setMessage( "{validator.positiveOrZero}" );
		PositiveOrZero m = descriptorBuilder.build().getAnnotation();

		PositiveOrZeroValidatorForCharSequence constraint = new PositiveOrZeroValidatorForCharSequence();
		constraint.initialize( m );

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "15", null ) );
		assertTrue( constraint.isValid( "15.0", null ) );
		assertTrue( constraint.isValid( "0", null ) );
		assertFalse( constraint.isValid( "-10", null ) );
		assertFalse( constraint.isValid( "-14.99", null ) );

		// number format exception
		assertFalse( constraint.isValid( "15l", null ) );
	}

	@Test
	public void testIsValidNegativeValidator() {
		ConstraintAnnotationDescriptor.Builder<Negative> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Negative.class );
		descriptorBuilder.setMessage( "{validator.negative}" );
		Negative m = descriptorBuilder.build().getAnnotation();

		NegativeValidatorForCharSequence constraint = new NegativeValidatorForCharSequence();
		constraint.initialize( m );

		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( "15", null ) );
		assertFalse( constraint.isValid( "15.0", null ) );
		assertFalse( constraint.isValid( "0", null ) );
		assertTrue( constraint.isValid( "-10", null ) );
		assertTrue( constraint.isValid( "-14.99", null ) );

		// number format exception
		assertFalse( constraint.isValid( "15l", null ) );
	}

	@Test
	public void testIsValidNegativeOrZeroValidator() {
		ConstraintAnnotationDescriptor.Builder<NegativeOrZero> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( NegativeOrZero.class );
		descriptorBuilder.setMessage( "{validator.negativeOrZero}" );
		NegativeOrZero m = descriptorBuilder.build().getAnnotation();

		NegativeOrZeroValidatorForCharSequence constraint = new NegativeOrZeroValidatorForCharSequence();
		constraint.initialize( m );

		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( "15", null ) );
		assertFalse( constraint.isValid( "15.0", null ) );
		assertTrue( constraint.isValid( "0", null ) );
		assertTrue( constraint.isValid( "-10", null ) );
		assertTrue( constraint.isValid( "-14.99", null ) );

		// number format exception
		assertFalse( constraint.isValid( "15l", null ) );
	}
}
