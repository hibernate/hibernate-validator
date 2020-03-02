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

import java.math.BigDecimal;

import jakarta.validation.Validator;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.AbstractMinValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForShort;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.AbstractDecimalMinValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForByte;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForShort;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 * @author Xavier Sosnovsky
 * @author Marko Bekhta
 */
public class MinValidatorForNumberTest extends BaseMinMaxValidatorForNumberTest {

	@Test
	public void testIsValidMinValidator() {
		ConstraintAnnotationDescriptor.Builder<Min> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( Min.class );
		descriptorBuilder.setAttribute( "value", 15L );
		descriptorBuilder.setMessage( "{validator.min}" );
		Min m = descriptorBuilder.build().getAnnotation();

		testMin( m, true );
	}

	@Test
	public void testIsValidDecimalMinValidator() {
		ConstraintAnnotationDescriptor.Builder<DecimalMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMin.class );
		descriptorBuilder.setAttribute( "value", "1500E-2" );
		descriptorBuilder.setMessage( "{validator.min}" );
		DecimalMin m = descriptorBuilder.build().getAnnotation();

		testDecimalMin( m, true );
	}

	@Test
	public void testIsValidDecimalMinWithDecimalFractionInConstraint() {
		class Foo {
			@DecimalMin("15.0001")
			private final Number num;

			Foo(final Number num) {
				this.num = num;
			}
		}

		Validator validator = getValidator();

		assertThat( validator.validate( new Foo( 15 ) ) ).containsOnlyViolations( violationOf( DecimalMin.class ) );
		assertNoViolations( validator.validate( new Foo( 16 ) ) );

		assertThat( validator.validate( new Foo( 15.00001 ) ) ).containsOnlyViolations( violationOf( DecimalMin.class ) );
		assertNoViolations( validator.validate( new Foo( 15.01 ) ) );

		assertThat( validator.validate( new Foo( BigDecimal.valueOf( 15.00001 ) ) ) ).containsOnlyViolations( violationOf( DecimalMin.class ) );
		assertNoViolations( validator.validate( new Foo( BigDecimal.valueOf( 15.01 ) ) ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInitializeDecimalMinWithInvalidValue() {
		ConstraintAnnotationDescriptor.Builder<DecimalMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMin.class );
		descriptorBuilder.setAttribute( "value", "foobar" );
		descriptorBuilder.setMessage( "{validator.min}" );
		DecimalMin m = descriptorBuilder.build().getAnnotation();

		DecimalMinValidatorForNumber constraint = new DecimalMinValidatorForNumber();
		constraint.initialize( m );
	}

	@Test
	@TestForIssue(jiraKey = "HV-256")
	public void testIsValidDecimalMinExclusive() {
		boolean inclusive = false;
		ConstraintAnnotationDescriptor.Builder<DecimalMin> descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( DecimalMin.class );
		descriptorBuilder.setAttribute( "value", "1500E-2" );
		descriptorBuilder.setAttribute( "inclusive", inclusive );
		descriptorBuilder.setMessage( "{validator.min}" );
		DecimalMin m = descriptorBuilder.build().getAnnotation();

		testDecimalMin( m, inclusive );
	}

	private void testDecimalMin(DecimalMin m, boolean inclusive) {
		AbstractDecimalMinValidator constraint = new DecimalMinValidatorForNumber();
		constraint.initialize( m );
		testNumberValidator( constraint, inclusive, false );

		constraint = new DecimalMinValidatorForBigDecimal();
		constraint.initialize( m );
		testValidatorBigDecimal( constraint, inclusive, false );

		constraint = new DecimalMinValidatorForBigInteger();
		constraint.initialize( m );
		testValidatorBigInteger( constraint, inclusive, false );

		constraint = new DecimalMinValidatorForByte();
		constraint.initialize( m );
		testValidatorByte( constraint, inclusive, false );

		constraint = new DecimalMinValidatorForShort();
		constraint.initialize( m );
		testValidatorShort( constraint, inclusive, false );

		constraint = new DecimalMinValidatorForInteger();
		constraint.initialize( m );
		testValidatorInteger( constraint, inclusive, false );

		constraint = new DecimalMinValidatorForLong();
		constraint.initialize( m );
		testValidatorLong( constraint, inclusive, false );

		constraint = new DecimalMinValidatorForFloat();
		constraint.initialize( m );
		testValidatorFloat( constraint, inclusive, false );

		constraint = new DecimalMinValidatorForDouble();
		constraint.initialize( m );
		testValidatorDouble( constraint, inclusive, false );
	}

	private void testMin(Min m, boolean inclusive) {
		AbstractMinValidator constraint = new MinValidatorForNumber();
		constraint.initialize( m );
		testNumberValidator( constraint, inclusive, false );

		constraint = new MinValidatorForBigDecimal();
		constraint.initialize( m );
		testValidatorBigDecimal( constraint, inclusive, false );

		constraint = new MinValidatorForBigInteger();
		constraint.initialize( m );
		testValidatorBigInteger( constraint, inclusive, false );

		constraint = new MinValidatorForByte();
		constraint.initialize( m );
		testValidatorByte( constraint, inclusive, false );

		constraint = new MinValidatorForShort();
		constraint.initialize( m );
		testValidatorShort( constraint, inclusive, false );

		constraint = new MinValidatorForInteger();
		constraint.initialize( m );
		testValidatorInteger( constraint, inclusive, false );

		constraint = new MinValidatorForLong();
		constraint.initialize( m );
		testValidatorLong( constraint, inclusive, false );

		constraint = new MinValidatorForFloat();
		constraint.initialize( m );
		testValidatorFloat( constraint, inclusive, false );

		constraint = new MinValidatorForDouble();
		constraint.initialize( m );
		testValidatorDouble( constraint, inclusive, false );
	}

}
