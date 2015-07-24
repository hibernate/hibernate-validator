/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import org.hibernate.validator.internal.constraintvalidators.bv.DecimalMaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.MaxValidatorForNumber;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

import javax.validation.ConstraintValidator;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Max;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 * @author Xavier Sosnovsky
 */
public class MaxValidatorForNumberTest {

	@Test
	public void testIsValidMax() {
		AnnotationDescriptor<Max> descriptor = new AnnotationDescriptor<Max>( Max.class );
		descriptor.setValue( "value", 15L );
		descriptor.setValue( "message", "{validator.max}" );
		Max m = AnnotationFactory.create( descriptor );

		MaxValidatorForNumber constraint = new MaxValidatorForNumber();
		constraint.initialize( m );
		testMaxValidator( constraint, true );
	}

	@Test
	public void testIsValidDecimalMax() {
		AnnotationDescriptor<DecimalMax> descriptor = new AnnotationDescriptor<DecimalMax>( DecimalMax.class );
		descriptor.setValue( "value", "15.0E0" );
		descriptor.setValue( "message", "{validator.max}" );
		DecimalMax m = AnnotationFactory.create( descriptor );

		DecimalMaxValidatorForNumber constraint = new DecimalMaxValidatorForNumber();
		constraint.initialize( m );
		testMaxValidator( constraint, true );
	}

	@Test
	public void testInitializeDecimalMaxWithInvalidValue() {
		AnnotationDescriptor<DecimalMax> descriptor = new AnnotationDescriptor<DecimalMax>( DecimalMax.class );
		descriptor.setValue( "value", "foobar" );
		descriptor.setValue( "message", "{validator.max}" );
		DecimalMax m = AnnotationFactory.create( descriptor );

		DecimalMaxValidatorForNumber constraint = new DecimalMaxValidatorForNumber();
		try {
			constraint.initialize( m );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			// success
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-256")
	public void testIsValidDecimalMaxExclusive() {
		boolean inclusive = false;
		AnnotationDescriptor<DecimalMax> descriptor = new AnnotationDescriptor<DecimalMax>( DecimalMax.class );
		descriptor.setValue( "value", "15.0E0" );
		descriptor.setValue( "inclusive", inclusive );
		descriptor.setValue( "message", "{validator.max}" );
		DecimalMax m = AnnotationFactory.create( descriptor );

		DecimalMaxValidatorForNumber constraint = new DecimalMaxValidatorForNumber();
		constraint.initialize( m );
		testMaxValidator( constraint, inclusive );
	}

	private void testMaxValidator(ConstraintValidator<?, Number> constraint, boolean inclusive) {
		byte b = 1;
		Byte bWrapper = 127;

		if ( inclusive ) {
			assertTrue( constraint.isValid( 15L, null ) );
			assertTrue( constraint.isValid( 15, null ) );
			assertTrue( constraint.isValid( 15.0, null ) );
		}
		else {
			assertFalse( constraint.isValid( 15L, null ) );
			assertFalse( constraint.isValid( 15, null ) );
			assertFalse( constraint.isValid( 15.0, null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( b, null ) );
		assertTrue( constraint.isValid( BigDecimal.valueOf( -156000000000.0 ), null ) );
		assertTrue( constraint.isValid( BigInteger.valueOf( -10000000L ), null ) );
		assertTrue( constraint.isValid( 10, null ) );
		assertTrue( constraint.isValid( 14.99, null ) );
		assertTrue( constraint.isValid( -14.99, null ) );
		assertTrue( constraint.isValid( Double.NEGATIVE_INFINITY, null ) );
		assertTrue( constraint.isValid( Float.NEGATIVE_INFINITY, null ) );
		assertFalse( constraint.isValid( 20, null ) );
		assertFalse( constraint.isValid( bWrapper, null ) );
		assertFalse( constraint.isValid( BigDecimal.valueOf( 156000000000.0 ), null ) );
		assertFalse( constraint.isValid( BigInteger.valueOf( 10000000L ), null ) );
		assertFalse( constraint.isValid( Double.POSITIVE_INFINITY, null ) );
		assertFalse( constraint.isValid( Float.POSITIVE_INFINITY, null ) );
		assertFalse( constraint.isValid( Double.NaN, null ) );
		assertFalse( constraint.isValid( Float.NaN, null ) );
	}
}
