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
package org.hibernate.validator.test.internal.util;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.util.IdentitySet;

/**
 * @author Hardy Ferentschik
 */
public class IdentitySetTest {

	@SuppressWarnings("unchecked")
	@Test
	public void testAddIdenticalInstance() {
		Set identitySet = new IdentitySet();
		Set hashSet = new HashSet();
		assertTrue( identitySet.size() == 0 );
		assertTrue( hashSet.size() == 0 );

		Object o1 = new Object() {
			int counter = 0;

			public int hashCode() {
				return counter++;
			}

			public boolean equals() {
				return false;
			}
		};
		identitySet.add( o1 );
		hashSet.add( o1 );
		assertTrue( identitySet.size() == 1 );
		assertTrue( hashSet.size() == 1 );

		identitySet.add( o1 );
		hashSet.add( o1 );
		assertTrue( identitySet.size() == 1 );
		assertTrue( hashSet.size() == 2 );

		Object o2 = new Object() {
			int counter = 0;

			public int hashCode() {
				return counter++;
			}

			public boolean equals() {
				return false;
			}
		};
		identitySet.add( o2 );
		hashSet.add( o2 );
		assertTrue( identitySet.size() == 2 );
		assertTrue( hashSet.size() == 3 );
	}
}
