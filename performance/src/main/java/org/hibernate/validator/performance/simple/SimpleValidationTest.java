/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.performance.simple;

import java.util.Random;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
public class SimpleValidationTest {
	private static final String[] names = {
			null,
			"Jacob",
			"Isabella",
			"Ethan",
			"Sophia",
			"Michael",
			"Emma",
			"Jayden",
			"Olivia",
			"William"
	};

	private static Validator validator;
	private static Random random;

	@BeforeClass
	public static void setUpValidatorFactory() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
		random = new Random();
	}

	@Test
	public void testSimpleBeanValidation() {
		DriverSetup driverSetup = new DriverSetup();
		Set<ConstraintViolation<Driver>> violations = validator.validate( driverSetup.getDriver() );
		assertEquals( driverSetup.getExpectedViolationCount(), violations.size() );
	}

	@Test
	public void testSimpleBeanValidationRecreatingValidatorFactory() {
		DriverSetup driverSetup = new DriverSetup();
		Validator localValidator = Validation.buildDefaultValidatorFactory().getValidator();
		Set<ConstraintViolation<Driver>> violations = localValidator.validate( driverSetup.getDriver() );
		assertEquals( driverSetup.getExpectedViolationCount(), violations.size() );
	}

	public class Driver {
		@NotNull
		String name;

		@Min(18)
		int age;

		@AssertTrue
		private boolean hasDrivingLicense;

		public Driver(String name, int age, boolean hasDrivingLicense) {
			this.name = name;
			this.age = age;
			this.hasDrivingLicense = hasDrivingLicense;
		}

		@Override
		public String toString() {
			final StringBuilder sb = new StringBuilder();
			sb.append( "Driver" );
			sb.append( "{name='" ).append( name ).append( '\'' );
			sb.append( ", age=" ).append( age );
			sb.append( ", hasDrivingLicense=" ).append( hasDrivingLicense );
			sb.append( '}' );
			return sb.toString();
		}
	}

	private class DriverSetup {
		private int expectedViolationCount;
		private Driver driver;

		public DriverSetup() {
			expectedViolationCount = 0;

			String name = names[random.nextInt( 10 )];
			if ( name == null ) {
				expectedViolationCount++;
			}

			int randomAge = random.nextInt( 100 );
			if ( randomAge < 18 ) {
				expectedViolationCount++;
			}

			int rand = random.nextInt( 2 );
			boolean hasLicense = rand == 1;
			if ( !hasLicense ) {
				expectedViolationCount++;
			}

			driver = new Driver( name, randomAge, hasLicense );
		}

		public int getExpectedViolationCount() {
			return expectedViolationCount;
		}

		public Driver getDriver() {
			return driver;
		}
	}
}



