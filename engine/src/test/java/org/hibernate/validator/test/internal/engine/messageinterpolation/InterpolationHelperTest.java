/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.escapeMessageParameter;
import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.isEscapeableChar;
import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.resolveToString;
import static org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper.unescapeMessageParameter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class InterpolationHelperTest {

	@Test
	public void testEscapeMessageParameter() {
		assertThat( escapeMessageParameter( null ) ).isNull();
		assertThat( escapeMessageParameter( "test" ) ).isEqualTo( "test" );
		assertThat( escapeMessageParameter( "{}" ) ).isEqualTo( "\\{\\}" );
		assertThat( escapeMessageParameter( "${\\}" ) ).isEqualTo( "\\$\\{\\\\\\}" );
	}

	@DataProvider(name = "escape")
	public static Object[][] escape() {
		return new Object[][] {
				{ null, null },
				{ "", "" },
				{ "test", "test" },
				{ "hello world 123!", "hello world 123!" },
				// each special character on its own
				{ "{", "\\{" },
				{ "}", "\\}" },
				{ "$", "\\$" },
				{ "\\", "\\\\" },
				// combinations
				{ "{}", "\\{\\}" },
				{ "${}", "\\$\\{\\}" },
				{ "a{b}c$d", "a\\{b\\}c\\$d" },
				{ "{{{{$$$$", "\\{\\{\\{\\{\\$\\$\\$\\$" },
				// pre-existing backslashes
				{ "\\{", "\\\\\\{" }, // \ {  ->  \\ \{
				{ "a\\b", "a\\\\b" }, // a \ b -> a \\ b
				{ "\\n", "\\\\n" }, // backslash + 'n' (not a newline)
				// specials at the edges of the string
				{ "{value}", "\\{value\\}" },
				{ "${a} {b} \\ c", "\\$\\{a\\} \\{b\\} \\\\ c" },
				// unicode around specials
				{ "héllo {wörld}", "héllo \\{wörld\\}" }
		};
	}

	@Test(dataProvider = "escape")
	public void testEscapeMessageParameter(String input, String expected) {
		assertThat( escapeMessageParameter( input ) ).isEqualTo( expected );
	}

	@Test
	public void testEscapeReturnsSameInstanceWhenNothingToEscape() {
		String plain = "no special characters here";
		assertThat( escapeMessageParameter( plain ) ).isSameAs( plain );
		assertThat( escapeMessageParameter( "" ) ).isEmpty();
	}

	@DataProvider(name = "unescape")
	public static Object[][] unescape() {
		return new Object[][] {
				{ "", "" },
				// bare specials without a backslash are left alone
				{ "{ } $ plain", "{ } $ plain" },
				// single escapes
				{ "\\{", "{" },
				{ "\\}", "}" },
				{ "\\$", "$" },
				{ "\\\\", "\\" },
				// mixed content
				{ "a\\{b\\}c\\$d\\\\e", "a{b}c$d\\e" },
				{ "\\$\\{\\}", "${}" },
				// backslash followed by a non-special character is kept as-is
				{ "\\n", "\\n" }, // backslash + 'n' (not a newline)
				{ "\\t", "\\t" },
				{ "a \\ b", "a \\ b" },
				// trailing lone backslash is preserved
				{ "abc\\", "abc\\" }
		};
	}

	@Test(dataProvider = "unescape")
	public void testUnescapeMessageParameter(String input, String expected) {
		assertThat( unescapeMessageParameter( input ) ).isEqualTo( expected );
	}

	@Test
	public void testUnescapeReturnsSameInstanceWhenNoBackslash() {
		String message = "{ } $ no backslashes";
		assertThat( unescapeMessageParameter( message ) ).isSameAs( message );
	}

	@Test(dataProvider = "escape")
	public void testUnescapeIsInverseOfEscape(String original, String escaped) {
		if ( original == null ) {
			return;
		}
		assertThat( unescapeMessageParameter( escaped ) ).isEqualTo( original );
	}

	@Test
	public void testUnescapeIsNotIdempotent() {
		// chars: \ \ {  =>  first pass yields \ {, second pass yields {
		// documents that unescaping must be applied exactly once at the end of interpolation
		String input = "\\\\{";
		assertThat( unescapeMessageParameter( input ) ).isEqualTo( "\\{" );
		assertThat( unescapeMessageParameter( unescapeMessageParameter( input ) ) ).isEqualTo( "{" );
	}

	@Test
	public void testUnescapeNullThrows() {
		// pins current behavior: escape(null) returns null but unescape(null) NPEs.
		// If you add a symmetric null guard, flip this to assertThat( unescapeMessageParameter( null ) ).isNull();
		assertThatThrownBy( () -> unescapeMessageParameter( null ) ).isInstanceOf( NullPointerException.class );
	}

	@DataProvider(name = "chars")
	public static Object[][] chars() {
		return new Object[][] {
				{ '{', true },
				{ '}', true },
				{ '$', true },
				{ '\\', true },
				{ 'a', false },
				{ 'z', false },
				{ '0', false },
				{ '_', false },
				{ '-', false },
				{ '.', false },
				{ ' ', false },
				{ '\t', false },
				{ '\n', false },
				{ 'é', false },
				{ '€', false }
		};
	}

	@Test(dataProvider = "chars")
	public void testIsEscapeableChar(char c, boolean expected) {
		assertThat( isEscapeableChar( c ) ).isEqualTo( expected );
	}

	@DataProvider(name = "values")
	public static Object[][] values() {
		return new Object[][] {
				{ "plain", "plain" },
				{ Integer.valueOf( 42 ), "42" },
				{ Boolean.TRUE, "true" },
				// primitive arrays: populated and empty
				{ new boolean[] { true, false }, "[true, false]" },
				{ new char[] { 'a', 'b' }, "[a, b]" },
				{ new byte[] { 1, -2 }, "[1, -2]" },
				{ new short[] { (short) 1, (short) 2 }, "[1, 2]" },
				{ new int[] { 1, 2, 3 }, "[1, 2, 3]" },
				{ new long[] { 1L, 2L }, "[1, 2]" },
				{ new float[] { 1.5f }, "[1.5]" },
				{ new double[] { 0.1d, -2.0d }, "[0.1, -2.0]" },
				{ new boolean[] { }, "[]" },
				{ new int[] { }, "[]" },
				// object arrays, incl. null elements and empty
				{ new String[] { "a", "b" }, "[a, b]" },
				{ new Integer[] { 1, null }, "[1, null]" },
				{ new Object[] { }, "[]" }
		};
	}

	@Test(dataProvider = "values")
	public void testResolveToString(Object value, String expected) {
		assertThat( resolveToString( value ) ).isEqualTo( expected );
	}

	@Test
	public void testResolveToStringNestedStringArray() {
		assertThat( resolveToString( new String[][] { { "a", "b" }, { "c" } } ) ).isEqualTo( "[[a, b], [c]]" );
	}

	@Test
	public void testResolveToStringNestedPrimitiveArray() {
		assertThat( resolveToString( new int[][] { { 1, 2 }, { 3 } } ) ).isEqualTo( "[[1, 2], [3]]" );
	}

	@Test
	public void testResolveToStringNullThrows() {
		// pins current behavior (value.getClass() NPEs). Flip if you add a null guard or document @NonNull.
		assertThatThrownBy( () -> resolveToString( null ) ).isInstanceOf( NullPointerException.class );
	}
}
