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

import java.util.Date;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.FutureValidatorForDate;

public class FutureValidatorForDateTest {

	private static FutureValidatorForDate constraint;

	@BeforeClass
	public static void init() {
		constraint = new FutureValidatorForDate();
	}

	@Test
	public void testIsValid() {
		Date futureDate = getFutureDate();
		Date pastDate = getPastDate();
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( futureDate, null ) );
		assertFalse( constraint.isValid( pastDate, null ) );
	}

	private Date getFutureDate() {
		Date date = new Date();
		long timeStamp = date.getTime();
		date.setTime( timeStamp + 31557600000L );
		return date;
	}

	private Date getPastDate() {
		Date date = new Date();
		long timeStamp = date.getTime();
		date.setTime( timeStamp - 31557600000L );
		return date;
	}

}
