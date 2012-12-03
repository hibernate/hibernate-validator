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

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.MutableDateTime;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.FutureValidatorForReadableInstant;

/**
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public class FutureValidatorForReadableInstantTest {

	private static FutureValidatorForReadableInstant validator;

	@BeforeClass
	public static void init() {
		validator = new FutureValidatorForReadableInstant();
	}

	@Test
	public void testIsValidForInstant() {
		Instant future = new Instant().plus( 31557600000L );
		Instant past = new Instant().minus( 31557600000L );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( future, null ) );
		Assert.assertFalse( validator.isValid( past, null ) );
	}

	@Test
	public void testIsValidForDateTime() {
		DateTime future = new DateTime().plusYears( 1 );
		DateTime past = new DateTime().minusYears( 1 );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( future, null ) );
		Assert.assertFalse( validator.isValid( past, null ) );
	}

	@Test
	public void testIsValidForDateMidnight() {
		DateMidnight future = new DateMidnight().plusYears( 1 );
		DateMidnight past = new DateMidnight().minusYears( 1 );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( future, null ) );
		Assert.assertFalse( validator.isValid( past, null ) );
	}

	@Test
	public void testIsValidForMutableDateTime() {
		MutableDateTime future = new MutableDateTime();
		future.addYears( 1 );

		MutableDateTime past = new MutableDateTime();
		past.addYears( -1 );

		Assert.assertTrue( validator.isValid( null, null ) );
		Assert.assertTrue( validator.isValid( future, null ) );
		Assert.assertFalse( validator.isValid( past, null ) );
	}
}
