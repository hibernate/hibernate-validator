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
package org.hibernate.validator.test.impl.util;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import org.hibernate.validator.impl.util.ModUtil;

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
		List<Integer> digits = Arrays.asList( 7, 3 );
		assertTrue( ModUtil.passesMod10Test( digits, 1 ) );

		digits = Arrays.asList( 7, 9, 9, 2, 7, 3, 9, 8, 7, 1, 3 );
		assertTrue( ModUtil.passesMod10Test( digits, 2 ) );
	}

	@Test
	public void testFailMod10() throws Exception {
		List<Integer> digits = Arrays.asList( 7, 2 );
		assertFalse( ModUtil.passesMod10Test( digits, 1 ) );

		digits = Arrays.asList( 7, 9, 9, 2, 7, 3, 9, 8, 7, 1, 4 );
		assertFalse( ModUtil.passesMod10Test( digits, 2 ) );
	}

	@Test
	public void testPassMod11() throws Exception {
		List<Integer> digits = Arrays.asList( 2, 7 );
		assertTrue( ModUtil.passesMod11Test( digits, 11 ) );

		digits = Arrays.asList( 0, 3, 6, 5, 3, 2, 7 );
		assertTrue( ModUtil.passesMod11Test( digits, 11 ) );

		digits = Arrays.asList( 1, 3, 4, 2, 4, 1, 3, 1, 3, 0 );
		assertTrue( ModUtil.passesMod11Test( digits, 11 ) );
	}

	@Test
	public void testFailMod11() throws Exception {
		List<Integer> digits = Arrays.asList( 2, 6 );
		assertFalse( ModUtil.passesMod11Test( digits, 11 ) );

		digits = Arrays.asList( 0, 3, 6, 5, 3, 2, 1 );
		assertFalse( ModUtil.passesMod11Test( digits, 11 ) );
	}
}
