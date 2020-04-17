/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;

import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForNumber;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class MaxValidatorForStringTest {

	@Test
	public void testIsValidMax() {

		ConstraintAnnotationDescriptor.Builder<Max> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Max.class );
		descriptorBuilder.setAttribute( "value", 15L );
		descriptorBuilder.setMessage( "{validator.max}" );
		Max m = descriptorBuilder.build().getAnnotation();

		MaxValidatorForCharSequence constraint = new MaxValidatorForCharSequence();
		constraint.initialize( m );
		testMaxValidator( constraint, true );
	}

	@Test
	public void testIsValidDecimalMax() {

		ConstraintAnnotationDescriptor.Builder<DecimalMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMax.class );
		descriptorBuilder.setAttribute( "value", "15.0E0" );
		descriptorBuilder.setMessage( "{validator.max}" );
		DecimalMax m = descriptorBuilder.build().getAnnotation();

		DecimalMaxValidatorForCharSequence constraint = new DecimalMaxValidatorForCharSequence();
		constraint.initialize( m );
		testMaxValidator( constraint, true );
	}

	@Test
	public void testInitializeDecimalMaxWithInvalidValue() {

		ConstraintAnnotationDescriptor.Builder<DecimalMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMax.class );
		descriptorBuilder.setAttribute( "value", "foobar" );
		descriptorBuilder.setMessage( "{validator.max}" );
		DecimalMax m = descriptorBuilder.build().getAnnotation();

		DecimalMaxValidatorForNumber constraint = new DecimalMaxValidatorForNumber();
		try {
			constraint.initialize( m );
			fail();
		}
		catch (IllegalArgumentException e) {
			// success
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-256")
	public void testIsValidDecimalMaxExclusive() {
		boolean inclusive = false;
		ConstraintAnnotationDescriptor.Builder<DecimalMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMax.class );
		descriptorBuilder.setAttribute( "value", "15.0E0" );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		descriptorBuilder.setMessage( "{validator.max}" );
		DecimalMax m = descriptorBuilder.build().getAnnotation();

		DecimalMaxValidatorForCharSequence constraint = new DecimalMaxValidatorForCharSequence();
		constraint.initialize( m );
		testMaxValidator( constraint, inclusive );
	}

	private void testMaxValidator(ConstraintValidator<?, CharSequence> constraint, boolean inclusive) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( "15", null ) );
			assertTrue( constraint.isValid( "15.0", null ) );
		}
		else {
			assertFalse( constraint.isValid( "15", null ) );
			assertFalse( constraint.isValid( "15.0", null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "10", null ) );
		assertTrue( constraint.isValid( "14.99", null ) );
		assertTrue( constraint.isValid( "-14.99", null ) );
		assertFalse( constraint.isValid( "20", null ) );

		// HV-502
		assertTrue( constraint.isValid( new MyCustomStringImpl( "10" ), null ) );

		//number format exception
		assertFalse( constraint.isValid( "15l", null ) );
	}
}
