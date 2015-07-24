/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import org.hibernate.validator.internal.constraintvalidators.bv.DecimalMinValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.bv.DecimalMinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.MinValidatorForCharSequence;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

import javax.validation.ConstraintValidator;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class MinValidatorForStringTest {

	@Test
	public void testIsValidMinValidator() {
		AnnotationDescriptor<Min> descriptor = new AnnotationDescriptor<Min>( Min.class );
		descriptor.setValue( "value", 15L );
		descriptor.setValue( "message", "{validator.min}" );
		Min m = AnnotationFactory.create( descriptor );

		MinValidatorForCharSequence constraint = new MinValidatorForCharSequence();
		constraint.initialize( m );
		testMinValidator( constraint, true );
	}

	@Test
	public void testIsValidDecimalMinValidator() {
		AnnotationDescriptor<DecimalMin> descriptor = new AnnotationDescriptor<DecimalMin>( DecimalMin.class );
		descriptor.setValue( "value", "1500E-2" );
		descriptor.setValue( "message", "{validator.min}" );
		DecimalMin m = AnnotationFactory.create( descriptor );

		DecimalMinValidatorForCharSequence constraint = new DecimalMinValidatorForCharSequence();
		constraint.initialize( m );
		testMinValidator( constraint, true );
	}

	@Test
	public void testInitializeDecimalMaxWithInvalidValue() {

		AnnotationDescriptor<DecimalMin> descriptor = new AnnotationDescriptor<DecimalMin>( DecimalMin.class );
		descriptor.setValue( "value", "foobar" );
		descriptor.setValue( "message", "{validator.min}" );
		DecimalMin m = AnnotationFactory.create( descriptor );

		DecimalMinValidatorForNumber constraint = new DecimalMinValidatorForNumber();
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
	public void testIsValidDecimalMinExclusive() {
		boolean inclusive = false;
		AnnotationDescriptor<DecimalMin> descriptor = new AnnotationDescriptor<DecimalMin>( DecimalMin.class );
		descriptor.setValue( "value", "1500E-2" );
		descriptor.setValue( "inclusive", inclusive );
		descriptor.setValue( "message", "{validator.min}" );
		DecimalMin m = AnnotationFactory.create( descriptor );

		DecimalMinValidatorForCharSequence constraint = new DecimalMinValidatorForCharSequence();
		constraint.initialize( m );
		testMinValidator( constraint, inclusive );
	}

	private void testMinValidator(ConstraintValidator<?, CharSequence> constraint, boolean inclusive) {
		if ( inclusive ) {
			assertTrue( constraint.isValid( "15", null ) );
			assertTrue( constraint.isValid( "15.0", null ) );
		}
		else {
			assertFalse( constraint.isValid( "15", null ) );
			assertFalse( constraint.isValid( "15.0", null ) );
		}

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "20", null ) );
		assertFalse( constraint.isValid( "10", null ) );
		assertFalse( constraint.isValid( "14.99", null ) );
		assertFalse( constraint.isValid( "-14.99", null ) );

		// HV-502
		assertTrue( constraint.isValid( new MyCustomStringImpl( "20" ), null ) );

		//number format exception
		assertFalse( constraint.isValid( "15l", null ) );
	}
}
