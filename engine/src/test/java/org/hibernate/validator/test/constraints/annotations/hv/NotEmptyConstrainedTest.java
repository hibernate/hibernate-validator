/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
@SuppressWarnings("deprecation")
public class NotEmptyConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void testNotEmpty() {
		Map<String, String> map = new HashMap<>();
		map.put( "", "" );
		Foo foo = new Foo( "foo", new Object[] { "" }, new boolean[] { true }, new byte[] { 1 },
				new char[] { 'c' }, new double[] { 1.0 }, new float[] { 1.0f }, new int[] { 1 },
				new long[] { 1L }, new short[] { 1 }, Arrays.asList( "" ), map
		);
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void testNotEmptyInvalid() {
		Foo foo = new Foo( "", new Object[] { }, new boolean[] { }, new byte[] { },
				new char[] { }, new double[] { }, new float[] { }, new int[] { },
				new long[] { }, new short[] { }, Collections.emptyList(), Collections.emptyMap()
		);
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NotEmpty.class ).withProperty( "string" ),
				violationOf( NotEmpty.class ).withProperty( "objects" ),
				violationOf( NotEmpty.class ).withProperty( "booleans" ),
				violationOf( NotEmpty.class ).withProperty( "bytes" ),
				violationOf( NotEmpty.class ).withProperty( "chars" ),
				violationOf( NotEmpty.class ).withProperty( "doubles" ),
				violationOf( NotEmpty.class ).withProperty( "floats" ),
				violationOf( NotEmpty.class ).withProperty( "ints" ),
				violationOf( NotEmpty.class ).withProperty( "longs" ),
				violationOf( NotEmpty.class ).withProperty( "shorts" ),
				violationOf( NotEmpty.class ).withProperty( "collection" ),
				violationOf( NotEmpty.class ).withProperty( "map" )
		);
	}

	private static class Foo {

		@NotEmpty
		private final String string;

		@NotEmpty
		private final Object[] objects;

		@NotEmpty
		private final boolean[] booleans;

		@NotEmpty
		private final byte[] bytes;

		@NotEmpty
		private final char[] chars;

		@NotEmpty
		private final double[] doubles;

		@NotEmpty
		private final float[] floats;

		@NotEmpty
		private final int[] ints;

		@NotEmpty
		private final long[] longs;

		@NotEmpty
		private final short[] shorts;

		@NotEmpty
		private final Collection<String> collection;

		@NotEmpty
		private final Map<String, String> map;

		public Foo(String string, Object[] objects, boolean[] booleans, byte[] bytes,
				char[] chars, double[] doubles, float[] floats, int[] ints,
				long[] longs, short[] shorts, Collection<String> collection,
				Map<String, String> map) {
			this.string = string;
			this.objects = objects;
			this.booleans = booleans;
			this.bytes = bytes;
			this.chars = chars;
			this.doubles = doubles;
			this.floats = floats;
			this.ints = ints;
			this.longs = longs;
			this.shorts = shorts;
			this.collection = collection;
			this.map = map;
		}
	}
}
