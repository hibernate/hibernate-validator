// $Id
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.constraints;

import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class PastValidatorForDateTest {

	private static PastConstraintDateValidator constraint;

	@BeforeClass
	public static void init() {
		constraint = new PastConstraintDateValidator();
	}

	@Test
	public void testIsValid() {
		Date futureDate = getFutureDate();
		Date pastDate = getPastDate();
		assertTrue( constraint.isValid( null, null ) );
		assertTrue( constraint.isValid( pastDate, null ) );
		assertFalse( constraint.isValid( new Date(), null ) );
		assertFalse( constraint.isValid( futureDate, null ) );
	}

	private Date getFutureDate() {
		Date date = new Date();
		long timeStamp = date.getTime();
		date.setTime( timeStamp + 31557600000l );
		return date;
	}

	private Date getPastDate() {
		Date date = new Date();
		long timeStamp = date.getTime();
		date.setTime( timeStamp - 31557600000l );
		return date;
	}

}
