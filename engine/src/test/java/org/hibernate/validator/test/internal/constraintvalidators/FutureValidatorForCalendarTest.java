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

import java.util.Calendar;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.FutureValidatorForCalendar;

/**
 * @author Alaa Nassef
 * @author Hardy Ferentschik
 */
public class FutureValidatorForCalendarTest {

	private static FutureValidatorForCalendar constraint;

	@BeforeClass
	public static void init() {
		constraint = new FutureValidatorForCalendar();
	}

	@Test
	public void testIsValid() {
		Calendar futureDate = getFutureDate();
		Calendar pastDate = getPastDate();
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( futureDate, null ) );
		assertFalse( constraint.isValid( pastDate, null ) );
	}

	private Calendar getFutureDate() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get( Calendar.YEAR );
		cal.set( Calendar.YEAR, year + 1 );
		return cal;
	}

	private Calendar getPastDate() {
		Calendar cal = Calendar.getInstance();
		int year = cal.get( Calendar.YEAR );
		cal.set( Calendar.YEAR, year - 1 );
		return cal;
	}

}
