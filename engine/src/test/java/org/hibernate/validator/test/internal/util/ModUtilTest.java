/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.util.ModUtil;
import org.hibernate.validator.testutil.TestForIssue;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

/**
 * Tests for the {@code ModUtil}.
 *
 * @author Hardy Ferentschik
 */
public class ModUtilTest {

	@Test
	public void testPassLuhnSum() throws Exception {
		List<Integer> digits = Arrays.asList( 7 );
		assertEquals( ModUtil.calculateLuhnMod10Check( digits ), 5 );

		digits = Arrays.asList( 7, 9, 9, 2, 7, 3, 9, 8, 7, 1 );
		assertEquals( ModUtil.calculateLuhnMod10Check( digits ), 3 );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6 );
		assertEquals( ModUtil.calculateLuhnMod10Check( digits ), 9 );
	}

	@Test
	public void testFailLuhnSum() throws Exception {
		List<Integer> digits = Arrays.asList( 7 );
		assertFalse( ModUtil.calculateLuhnMod10Check( digits ) == 2 );

		digits = Arrays.asList( 7, 9, 9, 2, 7, 3, 9, 8, 7, 1 );
		assertFalse( ModUtil.calculateLuhnMod10Check( digits ) == 4 );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6 );
		assertFalse( ModUtil.calculateLuhnMod10Check( digits ) == 0 );
	}

	@Test
	public void testPassMod11Sum() throws Exception {
		List<Integer> digits = Arrays.asList( 2 );
		assertEquals( ModUtil.calculateMod11Check( digits ), 7 );

		digits = Arrays.asList( 0, 3, 6, 5, 3, 2 );
		assertEquals( ModUtil.calculateMod11Check( digits ), 7 );

		digits = Arrays.asList( 1, 3, 4, 2, 4, 1, 3, 1, 3 );
		assertEquals( ModUtil.calculateMod11Check( digits ), 10 );
	}

	@Test
	public void testFailMod11Sum() throws Exception {
		List<Integer> digits = Arrays.asList( 2 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 6 );

		digits = Arrays.asList( 0, 3, 6, 5, 3, 2 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 1 );

		digits = Arrays.asList( 1, 3, 4, 2, 4, 1, 3, 1, 3 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 9 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void testFailMod11SelfValidation() throws Exception {
		List<Integer> digits = Arrays.asList( 0 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 1 );

		digits = Arrays.asList( 0, 0, 0, 0, 0, 0 );
		assertEquals( ModUtil.calculateMod11Check( digits ), 11 );

		digits = Arrays.asList( 0, 0, 0, 0, 0, 0 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 1 );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 5 );

		digits = Arrays.asList( 3, 7, 8, 7, 9, 6, 9, 5, 0, 0 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 2 );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6, 5 );
		assertFalse( ModUtil.calculateMod11Check( digits ) == 2 );
	}

}
