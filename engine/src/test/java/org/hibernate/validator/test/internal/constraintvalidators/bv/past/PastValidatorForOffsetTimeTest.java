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

import java.time.OffsetTime;
import java.time.ZoneOffset;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForOffsetTime;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.past.PastValidatorForOffsetTime}.
 *
 * @author Khalid Alqinyah
 */
public class PastValidatorForOffsetTimeTest {

	private PastValidatorForOffsetTime constraint;

	@BeforeClass
	public void init() {
		constraint = new PastValidatorForOffsetTime();
	}

	@Test
	public void testIsValid() {
		assertTrue( constraint.isValid( null, null ), "null fails validation." );

		// Test allowed zone offsets (-18 to +18) with 1 hour increments
		for ( int i = -18; i <= 18; i++ ) {
			ZoneOffset offset = ZoneOffset.ofHours( i );
			OffsetTime now = OffsetTime.now( offset );
			int currentHour = now.getHour();
			int currentMinute = now.getMinute();
			OffsetTime future = now.plusMinutes( 1 );
			OffsetTime past = now.minusMinutes( 1 );

			// Avoid false negatives at 00:00
			if (!(currentHour == 0 && currentMinute == 0)) {
				assertTrue( constraint.isValid( past, null ), "Past OffsetTime '" + past + "' fails validation.");
			}

			// Avoid false negatives at 23:59
			if (!(currentHour == 23 && currentMinute == 59)) {
				assertFalse( constraint.isValid( future, null ), "Future OffsetTime '" + future + "' validated as past.");
			}
		}
	}
}
