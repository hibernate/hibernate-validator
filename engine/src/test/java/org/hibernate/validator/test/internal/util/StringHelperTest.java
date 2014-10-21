/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util;

import java.util.Arrays;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.util.StringHelper;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Unit test for {@link StringHelper}.
 *
 * @author Gunnar Morling
 */
public class StringHelperTest {

	@Test
	public void joinShouldReturnNullForNullIterable() {
		assertThat( StringHelper.join( (Iterable<?>) null, "," ) ).isNull();
		assertThat( StringHelper.join( (Object[]) null, "," ) ).isNull();
	}

	@Test
	public void joinShouldReturnEmptyStringForEmptyIterable() {
		assertThat( StringHelper.join( newArrayList(), "," ) ).isEmpty();
		assertThat( StringHelper.join( new Object[0], "," ) ).isEmpty();
	}

	@Test
	public void joinShouldReturnElementForIterableWithOneElement() {
		assertThat( StringHelper.join( Arrays.asList( "polar bear" ), "," ) ).isEqualTo( "polar bear" );
		assertThat( StringHelper.join( new String[] { "polar bear" }, "," ) ).isEqualTo( "polar bear" );
	}

	@Test
	public void joinShouldReturnElementJoinedElements() {
		assertThat( StringHelper.join( Arrays.asList( "polar bear", "giraffe", "guinea pig" ), " and " ) ).isEqualTo(
				"polar bear and giraffe and guinea pig"
		);
		assertThat( StringHelper.join( new String[] { "polar bear", "giraffe", "guinea pig" }, " and " ) ).isEqualTo(
				"polar bear and giraffe and guinea pig"
		);
	}

	@Test
	public void joinShouldInvokeToStringForObjects() {
		assertThat( StringHelper.join( Arrays.asList( new PolarBear(), new Giraffe() ), ", " ) ).isEqualTo(
				"polar bear, giraffe"
		);
		assertThat( StringHelper.join( new Object[] { new PolarBear(), new Giraffe() }, ", " ) ).isEqualTo(
				"polar bear, giraffe"
		);
	}

	@Test
	public void joinShouldPrintLiteralNullForNullValues() {
		assertThat( StringHelper.join( new Integer[] { 1, null, 2 }, "," ) ).isEqualTo( "1,null,2" );
	}

	@Test
	public void decapitalizeShouldReturnNullForNullString() {
		assertNull( StringHelper.decapitalize( null ) );
	}

	@Test
	public void decapitalizeShouldReturnEmptyStringForEmptyString() {
		assertEquals( StringHelper.decapitalize( "" ), "" );
	}

	@Test
	public void decapitalizeShouldReturnLowerCaseLetter() {
		assertEquals( StringHelper.decapitalize( "A" ), "a" );
	}

	@Test
	public void decapitalizeShouldReturnDecapizalizedWord() {
		assertEquals( StringHelper.decapitalize( "Giraffe" ), "giraffe" );
	}

	@Test
	public void decapitalizeShouldReturnSameWordForDecapizalizedWord() {
		assertEquals( StringHelper.decapitalize( "giraffe" ), "giraffe" );
	}

	@Test
	public void decapitalizeShouldReturnSameWordForWordWithSeveralLeadingCapitalLetters() {
		assertEquals( StringHelper.decapitalize( "GIRaffe" ), "GIRaffe" );
	}

	private static class PolarBear {
		@Override
		public String toString() {
			return "polar bear";
		}
	}

	private static class Giraffe {
		@Override
		public String toString() {
			return "giraffe";
		}
	}
}
