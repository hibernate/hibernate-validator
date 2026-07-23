/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.List;

import org.hibernate.validator.internal.util.ModUtil;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@code ModUtil}.
 *
 * @author Hardy Ferentschik
 */
public class ModUtilTest {

	@Test
	public void testPassLuhnSum() throws Exception {
		List<Integer> digits = Arrays.asList( 7 );
		assertEquals( 5, ModUtil.calculateLuhnMod10Check( digits ) );

		digits = Arrays.asList( 7, 9, 9, 2, 7, 3, 9, 8, 7, 1 );
		assertEquals( 3, ModUtil.calculateLuhnMod10Check( digits ) );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6 );
		assertEquals( 9, ModUtil.calculateLuhnMod10Check( digits ) );
	}

	@Test
	public void testFailLuhnSum() throws Exception {
		List<Integer> digits = Arrays.asList( 7 );
		assertNotEquals( 2, ModUtil.calculateLuhnMod10Check( digits ) );

		digits = Arrays.asList( 7, 9, 9, 2, 7, 3, 9, 8, 7, 1 );
		assertNotEquals( 4, ModUtil.calculateLuhnMod10Check( digits ) );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6 );
		assertNotEquals( 0, ModUtil.calculateLuhnMod10Check( digits ) );
	}

	@Test
	public void testPassMod11Sum() throws Exception {
		List<Integer> digits = Arrays.asList( 2 );
		assertEquals( 7, ModUtil.calculateMod11Check( digits ) );

		digits = Arrays.asList( 0, 3, 6, 5, 3, 2 );
		assertEquals( 7, ModUtil.calculateMod11Check( digits ) );

		digits = Arrays.asList( 1, 3, 4, 2, 4, 1, 3, 1, 3 );
		assertEquals( 10, ModUtil.calculateMod11Check( digits ) );
	}

	@Test
	public void testFailMod11Sum() throws Exception {
		List<Integer> digits = Arrays.asList( 2 );
		assertNotEquals( 6, ModUtil.calculateMod11Check( digits ) );

		digits = Arrays.asList( 0, 3, 6, 5, 3, 2 );
		assertNotEquals( 1, ModUtil.calculateMod11Check( digits ) );

		digits = Arrays.asList( 1, 3, 4, 2, 4, 1, 3, 1, 3 );
		assertNotEquals( 9, ModUtil.calculateMod11Check( digits ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void testFailMod11SelfValidation() throws Exception {
		List<Integer> digits = Arrays.asList( 0 );
		assertNotEquals( 1, ModUtil.calculateMod11Check( digits ) );
		assertNotEquals( 1, ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ) );

		digits = Arrays.asList( 0, 0, 0, 0, 0, 0 );
		assertEquals( 11, ModUtil.calculateMod11Check( digits ) );
		assertEquals( 11, ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ) );

		digits = Arrays.asList( 0, 0, 0, 0, 0, 0 );
		assertNotEquals( 1, ModUtil.calculateMod11Check( digits ) );
		assertNotEquals( 1, ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ) );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6 );
		assertNotEquals( 5, ModUtil.calculateMod11Check( digits ) );
		assertNotEquals( 5, ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ) );

		digits = Arrays.asList( 3, 7, 8, 7, 9, 6, 9, 5, 0, 0 );
		assertNotEquals( 2, ModUtil.calculateMod11Check( digits ) );
		assertNotEquals( 2, ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ) );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6, 5 );
		assertNotEquals( 2, ModUtil.calculateMod11Check( digits ) );
		assertNotEquals( 2, ModUtil.calculateModXCheckWithWeights( digits, 11, Integer.MAX_VALUE ) );
	}

}
