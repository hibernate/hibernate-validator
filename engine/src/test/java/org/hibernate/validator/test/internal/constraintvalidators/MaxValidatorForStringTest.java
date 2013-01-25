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

import javax.validation.ConstraintValidator;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Max;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.DecimalMaxValidatorForCharSequence;
import org.hibernate.validator.internal.constraintvalidators.DecimalMaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.MaxValidatorForCharSequence;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.TestForIssue;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class MaxValidatorForStringTest {

	@Test
	public void testIsValidMax() {

		AnnotationDescriptor<Max> descriptor = new AnnotationDescriptor<Max>( Max.class );
		descriptor.setValue( "value", 15L );
		descriptor.setValue( "message", "{validator.max}" );
		Max m = AnnotationFactory.create( descriptor );

		MaxValidatorForCharSequence constraint = new MaxValidatorForCharSequence();
		constraint.initialize( m );
		testMaxValidator( constraint, true );
	}

	@Test
	public void testIsValidDecimalMax() {

		AnnotationDescriptor<DecimalMax> descriptor = new AnnotationDescriptor<DecimalMax>( DecimalMax.class );
		descriptor.setValue( "value", "15.0E0" );
		descriptor.setValue( "message", "{validator.max}" );
		DecimalMax m = AnnotationFactory.create( descriptor );

		DecimalMaxValidatorForCharSequence constraint = new DecimalMaxValidatorForCharSequence();
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
