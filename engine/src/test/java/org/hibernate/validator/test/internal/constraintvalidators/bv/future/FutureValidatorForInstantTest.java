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
package org.hibernate.validator.test.internal.constraintvalidators.bv.future;

import java.time.Instant;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForInstant;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link org.hibernate.validator.internal.constraintvalidators.bv.future.FutureValidatorForInstant}.
 *
 * @author Khalid Alqinyah
 */
public class FutureValidatorForInstantTest {

	private FutureValidatorForInstant constraint;

	@BeforeClass
	public void init() {
		constraint = new FutureValidatorForInstant();
	}

	@Test
	public void testIsValid() {
		Instant future = Instant.now().plusSeconds( 3600 );
		Instant past = Instant.now().minusSeconds( 3600 );

		assertTrue( constraint.isValid( null, null ), "null fails validation." );
		assertTrue( constraint.isValid( future, null ), "Future Instant '" + future + "' fails validation.");
		assertFalse( constraint.isValid( past, null ), "Past Instant '" + past + "' validated as future.");
	}
}
