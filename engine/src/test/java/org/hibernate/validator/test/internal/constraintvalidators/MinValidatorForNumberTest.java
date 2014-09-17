/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.constraintvalidators;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.validation.ConstraintValidator;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.DecimalMinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.MinValidatorForNumber;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.TestForIssue;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 * @author Xavier Sosnovsky
 */
public class MinValidatorForNumberTest {

	@Test
	public void testIsValidMinValidator() {
		AnnotationDescriptor<Min> descriptor = new AnnotationDescriptor<Min>( Min.class );
		descriptor.setValue( "value", 15L );
		descriptor.setValue( "message", "{validator.min}" );
		Min m = AnnotationFactory.create( descriptor );

		MinValidatorForNumber constraint = new MinValidatorForNumber();
		constraint.initialize( m );
		testMinValidator( constraint, true );
	}

	@Test
	public void testIsValidDecimalMinValidator() {
		AnnotationDescriptor<DecimalMin> descriptor = new AnnotationDescriptor<DecimalMin>( DecimalMin.class );
		descriptor.setValue( "value", "1500E-2" );
		descriptor.setValue( "message", "{validator.min}" );
		DecimalMin m = AnnotationFactory.create( descriptor );

		DecimalMinValidatorForNumber constraint = new DecimalMinValidatorForNumber();
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

		DecimalMinValidatorForNumber constraint = new DecimalMinValidatorForNumber();
		constraint.initialize( m );
		testMinValidator( constraint, inclusive );
	}

	private void testMinValidator(ConstraintValidator<?, Number> constraint, boolean inclusive) {
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
		assertTrue( constraint.isValid( bWrapper, null ) );
		assertTrue( constraint.isValid( 20, null ) );
		assertTrue( constraint.isValid( BigDecimal.valueOf( 156000000000.0 ), null ) );
		assertTrue( constraint.isValid( BigInteger.valueOf( 10000000L ), null ) );
		assertTrue( constraint.isValid( Double.POSITIVE_INFINITY, null ) );
		assertTrue( constraint.isValid( Float.POSITIVE_INFINITY, null ) );
		assertFalse( constraint.isValid( b, null ) );
		assertFalse( constraint.isValid( BigDecimal.valueOf( -156000000000.0 ), null ) );
		assertFalse( constraint.isValid( BigInteger.valueOf( -10000000L ), null ) );
		assertFalse( constraint.isValid( 10, null ) );
		assertFalse( constraint.isValid( 14.99, null ) );
		assertFalse( constraint.isValid( -14.99, null ) );
		assertFalse( constraint.isValid( Double.NEGATIVE_INFINITY, null ) );
		assertFalse( constraint.isValid( Float.NEGATIVE_INFINITY, null ) );
		assertFalse( constraint.isValid( Double.NaN, null ) );
		assertFalse( constraint.isValid( Float.NaN, null ) );
	}
}
