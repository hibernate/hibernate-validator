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
package org.hibernate.validator.test.constraints.impl;

import javax.validation.ConstraintValidator;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.impl.DecimalMinValidatorForNumber;
import org.hibernate.validator.constraints.impl.DecimalMinValidatorForString;
import org.hibernate.validator.constraints.impl.MinValidatorForString;
import org.hibernate.validator.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.util.annotationfactory.AnnotationFactory;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class MinValidatorForStringTest {

	@Test
	public void testIsValidMinValidator() {
		AnnotationDescriptor<Min> descriptor = new AnnotationDescriptor<Min>( Min.class );
		descriptor.setValue( "value", 15l );
		descriptor.setValue( "message", "{validator.min}" );
		Min m = AnnotationFactory.create( descriptor );

		MinValidatorForString constraint = new MinValidatorForString();
		constraint.initialize( m );
		testMinValidator( constraint );
	}

	@Test
	public void testIsValidDecimalMinValidator() {
		AnnotationDescriptor<DecimalMin> descriptor = new AnnotationDescriptor<DecimalMin>( DecimalMin.class );
		descriptor.setValue( "value", "1500E-2" );
		descriptor.setValue( "message", "{validator.min}" );
		DecimalMin m = AnnotationFactory.create( descriptor );

		DecimalMinValidatorForString constraint = new DecimalMinValidatorForString();
		constraint.initialize( m );
		testMinValidator( constraint );
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

	private void testMinValidator(ConstraintValidator<?, String> constraint) {
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( "20", null ) );
		assertTrue( constraint.isValid( "15", null ) );
		assertTrue( constraint.isValid( "15.0", null ) );
		assertFalse( constraint.isValid( "10", null ) );
		assertFalse( constraint.isValid( "14.99", null ) );
		assertFalse( constraint.isValid( "-14.99", null ) );
		//number format exception
		assertFalse( constraint.isValid( "15l", null ) );
	}
}
