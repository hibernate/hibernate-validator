/**
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
package org.hibernate.validator.quickstart;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <p>
 * A module test that shows how to use the Bean Validation (BV) API to validate
 * the constraint annotations at the exemplary {@link Car} model class.
 * </p>
 * <p>
 * The interface {@link Validator} is the main entry point the BV API. The
 * test makes use of the <code>validate()</code> method of that interface, which
 * returns a set of <code>ConstraintViolation</code>s, that describe the
 * problems occurred during validation.
 * </p>
 * <p>
 * In case the object in question could be validated successfully this set will
 * be empty.
 * </p>
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class CarTest {

	/**
	 * The validator to be used for object validation. Will be retrieved once
	 * for all test methods.
	 */
	private static Validator validator;

	/**
	 * Retrieves the validator instance.
	 */
	@BeforeClass
	public static void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	/**
	 * One constraint violation due to the manufacturer field being null
	 * expected.
	 */
	@Test
	public void manufacturerIsNull() {
		Car car = new Car( null, "DD-AB-123", 4 );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "may not be null", constraintViolations.iterator().next().getMessage() );
	}

	/**
	 * One constraint violation due to the licensePlate field being too short
	 * expected.
	 */
	@Test
	public void licensePlateTooShort() {
		Car car = new Car( "Morris", "D", 4 );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "size must be between 2 and 14", constraintViolations.iterator().next().getMessage() );
	}

	/**
	 * One constraint violation due to the seatCount field being too low
	 * expected.
	 */
	@Test
	public void seatCountTooLow() {
		Car car = new Car( "Morris", "DD-AB-123", 1 );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 1, constraintViolations.size() );
		assertEquals( "must be greater than or equal to 2", constraintViolations.iterator().next().getMessage() );
	}

	/**
	 * No constraint violation expected, as all fields of the validated Car
	 * instance have proper values.
	 */
	@Test
	public void carIsValid() {
		Car car = new Car( "Morris", "DD-AB-123", 2 );

		Set<ConstraintViolation<Car>> constraintViolations = validator.validate( car );

		assertEquals( 0, constraintViolations.size() );
	}
}
