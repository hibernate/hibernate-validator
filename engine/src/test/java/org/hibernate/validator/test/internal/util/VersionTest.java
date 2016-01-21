/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util;

import org.hibernate.validator.internal.util.Version;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
/**
 * @author Gunnar Morling
 */
public class VersionTest {

	@Test
	public void java8() {
		assertEquals( Version.determineJavaRelease( "1.8" ), 8 );
	}

	@Test
	public void java9() {
		assertEquals( Version.determineJavaRelease( "9" ), 9 );
	}

	@Test
	public void unknown() {
		assertEquals( Version.determineJavaRelease( "abc" ), 6 );
	}

	@Test
	public void nullValue() {
		assertEquals( Version.determineJavaRelease( null ), 6 );
	}
}
