/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
