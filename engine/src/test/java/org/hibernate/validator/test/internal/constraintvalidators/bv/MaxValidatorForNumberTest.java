/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertFalse;

import java.math.BigDecimal;
import java.math.BigInteger;

import jakarta.validation.Validator;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;

import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.AbstractMaxValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MaxValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.AbstractDecimalMaxValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMaxValidatorForShort;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 * @author Xavier Sosnovsky
 * @author Marko Bekhta
 */
public class MaxValidatorForNumberTest extends BaseMinMaxValidatorForNumberTest {

	@Test
	public void testIsValidMax() {
		ConstraintAnnotationDescriptor.Builder<Max> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Max.class );
		descriptorBuilder.setAttribute( "value", 15L );
		descriptorBuilder.setMessage( "{validator.max}" );
		Max m = descriptorBuilder.build().getAnnotation();

		testMax( m, true );
	}

	@Test
	public void testIsValidDecimalMax() {
		ConstraintAnnotationDescriptor.Builder<DecimalMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMax.class );
		descriptorBuilder.setAttribute( "value", "15.0E0" );
		descriptorBuilder.setMessage( "{validator.max}" );
		DecimalMax m = descriptorBuilder.build().getAnnotation();

		testDecimalMax( m, true );
	}

	@Test
	public void testIsValidDecimalMaxWithDecimalFractionInConstraint() {
		class Foo {
			@DecimalMax("15.0001")
			private final Number num;

			Foo(final Number num) {
				this.num = num;
			}
		}

		Validator validator = getValidator();

		assertThat( validator.validate( new Foo( 16 ) ) ).containsOnlyViolations( violationOf( DecimalMax.class ) );
		assertNoViolations( validator.validate( new Foo( 15 ) ) );

		assertThat( validator.validate( new Foo( 15.01 ) ) ).containsOnlyViolations( violationOf( DecimalMax.class ) );
		assertNoViolations( validator.validate( new Foo( 15.00001 ) ) );

		assertThat( validator.validate( new Foo( BigDecimal.valueOf( 15.01 ) ) ) ).containsOnlyViolations( violationOf( DecimalMax.class ) );
		assertNoViolations( validator.validate( new Foo( BigDecimal.valueOf( 15.00001 ) ) ) );
	}

	@Test
	public void testIsValidDecimalMax1() {
		ConstraintAnnotationDescriptor.Builder<DecimalMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMax.class );
		descriptorBuilder.setAttribute( "value", Integer.toString( Integer.MAX_VALUE - 1 ) );
		DecimalMax m = descriptorBuilder.build().getAnnotation();

		DecimalMaxValidatorForNumber constraint = new DecimalMaxValidatorForNumber();
		constraint.initialize( m );
		assertFalse( constraint.isValid( Double.POSITIVE_INFINITY, null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInitializeDecimalMaxWithInvalidValue() {
		ConstraintAnnotationDescriptor.Builder<DecimalMax> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMax.class );
		descriptorBuilder.setAttribute( "value", "foobar" );
		descriptorBuilder.setMessage( "{validator.max}" );
		DecimalMax m = descriptorBuilder.build().getAnnotation();

		DecimalMaxValidatorForNumber constraint = new DecimalMaxValidatorForNumber();
		constraint.initialize( m );
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
		testDecimalMax( m, false );

	}

	@Test
	@TestForIssue(jiraKey = "HV-1699")
	public void testIsValidNumberForFloatingPointOrBigNumbersStoredAsNumber() {
		ConstraintAnnotationDescriptor.Builder<Max> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Max.class );
		descriptorBuilder.setAttribute( "value", 1L );
		Max m = descriptorBuilder.build().getAnnotation();
		MaxValidatorForNumber validator = new MaxValidatorForNumber();
		validator.initialize( m );

		assertFalse( validator.isValid( 1.01, null ) );
		assertFalse( validator.isValid( 1.01F, null ) );
		assertFalse( validator.isValid( new BigDecimal( "1.01" ), null ) );
		assertFalse( validator.isValid( new BigInteger( "2" ), null ) );
		assertFalse( validator.isValid( Double.POSITIVE_INFINITY, null ) );
		assertFalse( validator.isValid( Float.POSITIVE_INFINITY, null ) );
	}

	private void testDecimalMax(DecimalMax m, boolean inclusive) {
		AbstractDecimalMaxValidator constraint = new DecimalMaxValidatorForNumber();
		constraint.initialize( m );
		testNumberValidator( constraint, inclusive, true );

		constraint = new DecimalMaxValidatorForBigDecimal();
		constraint.initialize( m );
		testValidatorBigDecimal( constraint, inclusive, true );

		constraint = new DecimalMaxValidatorForBigInteger();
		constraint.initialize( m );
		testValidatorBigInteger( constraint, inclusive, true );

		constraint = new DecimalMaxValidatorForByte();
		constraint.initialize( m );
		testValidatorByte( constraint, inclusive, true );

		constraint = new DecimalMaxValidatorForShort();
		constraint.initialize( m );
		testValidatorShort( constraint, inclusive, true );

		constraint = new DecimalMaxValidatorForInteger();
		constraint.initialize( m );
		testValidatorInteger( constraint, inclusive, true );

		constraint = new DecimalMaxValidatorForLong();
		constraint.initialize( m );
		testValidatorLong( constraint, inclusive, true );

		constraint = new DecimalMaxValidatorForFloat();
		constraint.initialize( m );
		testValidatorFloat( constraint, inclusive, true );

		constraint = new DecimalMaxValidatorForDouble();
		constraint.initialize( m );
		testValidatorDouble( constraint, inclusive, true );
	}

	private void testMax(Max m, boolean inclusive) {
		AbstractMaxValidator constraint = new MaxValidatorForNumber();
		constraint.initialize( m );
		testNumberValidator( constraint, inclusive, true );

		constraint = new MaxValidatorForBigDecimal();
		constraint.initialize( m );
		testValidatorBigDecimal( constraint, inclusive, true );

		constraint = new MaxValidatorForBigInteger();
		constraint.initialize( m );
		testValidatorBigInteger( constraint, inclusive, true );

		constraint = new MaxValidatorForByte();
		constraint.initialize( m );
		testValidatorByte( constraint, inclusive, true );

		constraint = new MaxValidatorForShort();
		constraint.initialize( m );
		testValidatorShort( constraint, inclusive, true );

		constraint = new MaxValidatorForInteger();
		constraint.initialize( m );
		testValidatorInteger( constraint, inclusive, true );

		constraint = new MaxValidatorForLong();
		constraint.initialize( m );
		testValidatorLong( constraint, inclusive, true );

		constraint = new MaxValidatorForFloat();
		constraint.initialize( m );
		testValidatorFloat( constraint, inclusive, true );

		constraint = new MaxValidatorForDouble();
		constraint.initialize( m );
		testValidatorDouble( constraint, inclusive, true );
	}

}
