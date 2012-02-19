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
import javax.validation.constraints.Digits;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.DigitsValidatorForNumber;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class DigitsValidatorForNumberTest {

	@Test
	public void testIsValid() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 5 );
		descriptor.setValue( "fraction", 2 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );


		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( Byte.valueOf( "0" ), null ) );
		assertTrue( constraint.isValid( Double.valueOf( "500.2" ), null ) );

		assertTrue( constraint.isValid( new BigDecimal( "-12345.12" ), null ) );
		assertFalse( constraint.isValid( new BigDecimal( "-123456.12" ), null ) );
		assertFalse( constraint.isValid( new BigDecimal( "-123456.123" ), null ) );
		assertFalse( constraint.isValid( new BigDecimal( "-12345.123" ), null ) );
		assertFalse( constraint.isValid( new BigDecimal( "12345.123" ), null ) );

		assertTrue( constraint.isValid( Float.valueOf( "-000000000.22" ), null ) );
		assertFalse( constraint.isValid( Integer.valueOf( "256874" ), null ) );
		assertFalse( constraint.isValid( Double.valueOf( "12.0001" ), null ) );
	}

	@Test
	public void testIsValidZeroLength() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 0 );
		descriptor.setValue( "fraction", 0 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );


		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( Byte.valueOf( "0" ), null ) );
		assertFalse( constraint.isValid( Double.valueOf( "500.2" ), null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeIntegerLength() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", -1 );
		descriptor.setValue( "fraction", 1 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNegativeFractionLength() {

		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 1 );
		descriptor.setValue( "fraction", -1 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );
	}

	@Test
	public void testTrailingZerosAreTrimmed() {
		AnnotationDescriptor<Digits> descriptor = new AnnotationDescriptor<Digits>( Digits.class );
		descriptor.setValue( "integer", 12 );
		descriptor.setValue( "fraction", 3 );
		descriptor.setValue( "message", "{validator.digits}" );
		Digits p = AnnotationFactory.create( descriptor );

		DigitsValidatorForNumber constraint = new DigitsValidatorForNumber();
		constraint.initialize( p );

		assertTrue( constraint.isValid( 0.001d, null ) );
		assertTrue( constraint.isValid( 0.00100d, null ) );
		assertFalse( constraint.isValid( 0.0001d, null ) );
	}

}
