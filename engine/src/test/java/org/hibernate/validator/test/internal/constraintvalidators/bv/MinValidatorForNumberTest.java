/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.bv;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;

import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.BaseMinValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.BaseDecimalMinValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigDecimal;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForBigInteger;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForDouble;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForFloat;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForLong;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.decimal.DecimalMinValidatorForNumber;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
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
		AnnotationDescriptor<Min> descriptor = new AnnotationDescriptor<Min>( Min.class );
		descriptor.setValue( "value", 15L );
		descriptor.setValue( "message", "{validator.min}" );
		Min m = AnnotationFactory.create( descriptor );

		testMin( m, true );
	}

	@Test
	public void testIsValidDecimalMinValidator() {
		AnnotationDescriptor<DecimalMin> descriptor = new AnnotationDescriptor<DecimalMin>( DecimalMin.class );
		descriptor.setValue( "value", "1500E-2" );
		descriptor.setValue( "message", "{validator.min}" );
		DecimalMin m = AnnotationFactory.create( descriptor );

		testDecimalMin( m, true );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInitializeDecimalMinWithInvalidValue() {
		AnnotationDescriptor<DecimalMin> descriptor = new AnnotationDescriptor<DecimalMin>( DecimalMin.class );
		descriptor.setValue( "value", "foobar" );
		descriptor.setValue( "message", "{validator.min}" );
		DecimalMin m = AnnotationFactory.create( descriptor );

		DecimalMinValidatorForNumber constraint = new DecimalMinValidatorForNumber();
		constraint.initialize( m );
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

		testDecimalMin( m, inclusive );
	}

	private void testDecimalMin(DecimalMin m, boolean inclusive) {
		BaseDecimalMinValidator constraint = new DecimalMinValidatorForNumber();
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
		BaseMinValidator constraint = new MinValidatorForNumber();
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
