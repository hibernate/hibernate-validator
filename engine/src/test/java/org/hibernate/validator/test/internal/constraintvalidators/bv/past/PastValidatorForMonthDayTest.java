/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.constraintvalidators.bv.past;

import java.time.Month;
import java.time.MonthDay;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForMonthDay;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForMonthDay}.
 *
 * @author Khalid Alqinyah
 */
public class PastValidatorForMonthDayTest {

	private PastValidatorForMonthDay constraint;

	@BeforeClass
	public void init() {
		constraint = new PastValidatorForMonthDay();
	}

	@Test
	public void testIsValid() {
		Month currentMonth =  MonthDay.now().getMonth();
		MonthDay future = MonthDay.of( currentMonth.plus( 1 ), 15 );
		MonthDay past = MonthDay.of( currentMonth.minus( 1 ), 15 );

		assertTrue( constraint.isValid( null, null ), "null fails validation." );

		// If currentMonth is January, past month will be December, skip to avoid a false negative
		if ( currentMonth != Month.JANUARY ) {
			assertTrue( constraint.isValid( past, null ), "Past MonthDay '" + past + "' fails validation.");
		}

		// If currentMonth is December, future month will be January, skip to avoid a false negative
		if ( currentMonth != Month.DECEMBER ) {
			assertFalse( constraint.isValid( future, null ), "Future MonthDay '" + future + "' validated as past.");
		}
	}
}
