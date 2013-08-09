/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.util.Arrays;
import java.util.List;

import org.hibernate.validator.internal.util.ModUtil;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests for the {@code ModUtil}.
 *
 * @author Hardy Ferentschik
 */
public class ModUtilTest {
	@Test
	public void testPassMod10() throws Exception {
		List<Integer> digits = Arrays.asList( 7 );
		assertTrue( ModUtil.passesMod10Test( digits, 3, 1 ) );

		digits = Arrays.asList( 7, 9, 9, 2, 7, 3, 9, 8, 7, 1 );
		assertTrue( ModUtil.passesMod10Test( digits, 3, 2 ) );
	}

	@Test
	public void testFailMod10() throws Exception {
		List<Integer> digits = Arrays.asList( 7 );
		assertFalse( ModUtil.passesMod10Test( digits, 2, 1 ) );

		digits = Arrays.asList( 7, 9, 9, 2, 7, 3, 9, 8, 7, 1 );
		assertFalse( ModUtil.passesMod10Test( digits, 4, 2 ) );
	}

	@Test
	public void testPassMod11() throws Exception {
		List<Integer> digits = Arrays.asList( 2 );
//		assertTrue( ModUtil.passesMod11Test( digits, 7, 11 ) );
//
//		digits = Arrays.asList( 0, 3, 6, 5, 3, 2 );
//		assertTrue( ModUtil.passesMod11Test( digits, 7, 11 ) );

		digits = Arrays.asList( 1, 3, 4, 2, 4, 1, 3, 1, 3 );
		assertTrue( ModUtil.passesMod11Test( digits, 0, 11 ) );
	}

	@Test
	public void testFailMod11() throws Exception {
		List<Integer> digits = Arrays.asList( 2 );
		assertFalse( ModUtil.passesMod11Test( digits, 6, 11 ) );

		digits = Arrays.asList( 0, 3, 6, 5, 3, 2 );
		assertFalse( ModUtil.passesMod11Test( digits, 1, 11 ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-808")
	public void testFailMod11SelfValidation() throws Exception {
		List<Integer> digits = Arrays.asList( 0 );
		assertFalse( "'0-1' must be invalid", ModUtil.passesMod11Test( digits, 1, 11 ) );

		digits = Arrays.asList( 0, 0, 0, 0, 0, 0 );
		assertTrue( "'000000-0' must be valid", ModUtil.passesMod11Test( digits, 0, 11 ) );

		digits = Arrays.asList( 0, 0, 0, 0, 0, 0 );
		assertFalse( "'000000-1' must be invalid", ModUtil.passesMod11Test( digits, 1, 11 ) );

		digits = Arrays.asList( 3, 3, 1, 8, 1, 4, 2, 9, 6 );
		assertFalse( "'331814296-5' must be invalid", ModUtil.passesMod11Test( digits, 5, 11 ) );
	}
}
