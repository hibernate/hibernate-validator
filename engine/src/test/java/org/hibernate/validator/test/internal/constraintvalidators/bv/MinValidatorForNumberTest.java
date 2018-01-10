/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;

import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.AbstractMinValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.AbstractDecimalMinValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForNumber;
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
