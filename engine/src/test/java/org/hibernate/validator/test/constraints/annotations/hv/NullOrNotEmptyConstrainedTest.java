/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.NullOrNotEmpty;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;

import org.testng.annotations.Test;

public class NullOrNotEmptyConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void nullIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void notEmptyIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "foobar" ) );
		assertNoViolations( violations );
	}

	@Test
	public void emptyIsInvalid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "" ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NullOrNotEmpty.class )
		);
	}

	@Test
	public void blankIsValid() {
		Set<ConstraintViolation<Foo>> violations =
				validator.validate( new Foo( "   " ) );
		assertNoViolations( violations );
	}

	@Test
	public void nullCollectionIsValid() {
		Set<ConstraintViolation<Bar>> violations =
				validator.validate( new Bar( null ) );
		assertNoViolations( violations );
	}

	@Test
	public void notEmptyCollectionIsValid() {
		Set<ConstraintViolation<Bar>> violations =
				validator.validate( new Bar( Collections.singletonList( "a" ) ) );
		assertNoViolations( violations );
	}

	@Test
	public void emptyCollectionIsInvalid() {
		Set<ConstraintViolation<Bar>> violations =
				validator.validate( new Bar( Collections.emptyList() ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NullOrNotEmpty.class )
		);
	}

	@Test
	public void optionalWithValueIsValid() {
		Set<ConstraintViolation<Baz>> violations =
				validator.validate( new Baz( Optional.of( "foobar" ) ) );
		assertNoViolations( violations );
	}

	@Test
	public void emptyOptionalIsValid() {
		Set<ConstraintViolation<Baz>> violations =
				validator.validate( new Baz( Optional.empty() ) );
		assertNoViolations( violations );
	}

	@Test
	public void optionalWithEmptyValueIsInvalid() {
		Set<ConstraintViolation<Baz>> violations =
				validator.validate( new Baz( Optional.of( "" ) ) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NullOrNotEmpty.class )
		);
	}

	@Test
	public void nullArrayIsValid() {
		Set<ConstraintViolation<Arrays>> violations =
				validator.validate( new Arrays() );
		assertNoViolations( violations );
	}

	@Test
	public void emptyArrayIsInvalid() {
		Set<ConstraintViolation<Arrays>> violations =
				validator.validate( new Arrays(
						new String[0],
						new byte[0],
						new short[0],
						new int[0],
						new long[0],
						new float[0],
						new double[0],
						new boolean[0]
				) );
		assertThat( violations ).containsOnlyViolations(
				violationOf( NullOrNotEmpty.class ),
				violationOf( NullOrNotEmpty.class ),
				violationOf( NullOrNotEmpty.class ),
				violationOf( NullOrNotEmpty.class ),
				violationOf( NullOrNotEmpty.class ),
				violationOf( NullOrNotEmpty.class ),
				violationOf( NullOrNotEmpty.class ),
				violationOf( NullOrNotEmpty.class )
		);
	}

	@Test
	public void nonEmptyArrayIsValid() {
		Set<ConstraintViolation<Arrays>> violations =
				validator.validate( new Arrays(
						new String[] { "1" },
						new byte[] { 1 },
						new short[] { 1 },
						new int[] { 1 },
						new long[] { 1 },
						new float[] { 1.0f },
						new double[] { 1.0 },
						new boolean[] { true }
				) );
		assertNoViolations( violations );
	}

	private static class Foo {

		@NullOrNotEmpty
		private final String string;

		public Foo(String string) {
			this.string = string;
		}
	}

	private static class Bar {

		@NullOrNotEmpty
		private final List<String> list;

		public Bar(List<String> list) {
			this.list = list;
		}
	}

	private static class Baz {

		private final Optional<@NullOrNotEmpty String> string;

		public Baz(Optional<String> string) {
			this.string = string;
		}
	}

	private static class Arrays {
		@NullOrNotEmpty
		private final String[] strings;
		@NullOrNotEmpty
		private final byte[] bytes;
		@NullOrNotEmpty
		private final short[] shorts;
		@NullOrNotEmpty
		private final int[] ints;
		@NullOrNotEmpty
		private final long[] longs;
		@NullOrNotEmpty
		private final float[] floats;
		@NullOrNotEmpty
		private final double[] doubles;
		@NullOrNotEmpty
		private final boolean[] booleans;

		private Arrays(String[] strings, byte[] bytes, short[] shorts, int[] ints, long[] longs, float[] floats, double[] doubles, boolean[] booleans) {
			this.strings = strings;
			this.bytes = bytes;
			this.shorts = shorts;
			this.ints = ints;
			this.longs = longs;
			this.floats = floats;
			this.doubles = doubles;
			this.booleans = booleans;
		}

		public Arrays() {
			this.strings = null;
			this.bytes = null;
			this.shorts = null;
			this.ints = null;
			this.longs = null;
			this.floats = null;
			this.doubles = null;
			this.booleans = null;
		}
	}
}
