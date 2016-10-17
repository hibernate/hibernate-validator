/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.hibernate.validator.constraints.NumberPattern;
import org.hibernate.validator.internal.constraintvalidators.hv.NumberPatternValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

import org.testng.annotations.Test;

/**
 * Tests for {@link NumberPatternValidator} which implements {@link NumberPattern} constraint validator.
 *
 * @author Marko Bekhta
 */
public class NumberPatternValidatorTest {

	@Test
	public void testDefaultIsValid() {
		AnnotationDescriptor<NumberPattern> descriptor = new AnnotationDescriptor<>( NumberPattern.class );
		descriptor.setValue( "regexp", "[1]+(.[1]+)?" );
		descriptor.setValue( "numberFormat", "" );

		NumberPattern annotation = AnnotationFactory.create( descriptor );
		NumberPatternValidator constraint = new NumberPatternValidator();
		constraint.initialize( annotation );

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( 1, null ) );
		assertTrue( constraint.isValid( 1.1, null ) );
		assertTrue( constraint.isValid( 11L, null ) );
		assertTrue( constraint.isValid( new BigDecimal( "111.1111" ), null ) );
		assertFalse( constraint.isValid( new BigInteger( "1111110" ), null ) );
	}

	@Test
	public void testWithNumberFormatIsValid() {
		AnnotationDescriptor<NumberPattern> descriptor = new AnnotationDescriptor<>( NumberPattern.class );
		descriptor.setValue( "regexp", "[1]+" );
		descriptor.setValue( "numberFormat", "###" );

		NumberPatternValidator constraint = new NumberPatternValidator();
		constraint.initialize( AnnotationFactory.create( descriptor ) );

		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( 1, null ) );
		assertTrue( constraint.isValid( 1.1, null ) );
		assertTrue( constraint.isValid( 11L, null ) );
		assertTrue( constraint.isValid( new BigDecimal( "111.1111" ), null ) );
		assertFalse( constraint.isValid( new BigInteger( "1111110" ), null ) );

		descriptor = new AnnotationDescriptor<>( NumberPattern.class );
		descriptor.setValue( "regexp", "[1]+(.[1]+)" );
		descriptor.setValue( "numberFormat", "###.##" );

		constraint.initialize( AnnotationFactory.create( descriptor ) );

		assertTrue( constraint.isValid( null, null ) );
		assertFalse( constraint.isValid( 1, null ) );
		assertTrue( constraint.isValid( 1.1, null ) );
		assertFalse( constraint.isValid( 11L, null ) );
		assertTrue( constraint.isValid( new BigDecimal( "111.1111" ), null ) );
		assertFalse( constraint.isValid( new BigInteger( "1111110" ), null ) );

	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidRegexpValue() {
		AnnotationDescriptor<NumberPattern> descriptor = new AnnotationDescriptor<>( NumberPattern.class );
		descriptor.setValue( "regexp", "[" );
		descriptor.setValue( "numberFormat", "" );

		NumberPattern p = AnnotationFactory.create( descriptor );
		NumberPatternValidator constraint = new NumberPatternValidator();
		constraint.initialize( p );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidFormatValue() {
		AnnotationDescriptor<NumberPattern> descriptor = new AnnotationDescriptor<>( NumberPattern.class );
		descriptor.setValue( "regexp", "[1]+(.[1]+)" );
		descriptor.setValue( "numberFormat", "something.went.wrong.here" );

		NumberPattern p = AnnotationFactory.create( descriptor );
		NumberPatternValidator constraint = new NumberPatternValidator();
		constraint.initialize( p );
	}

}
