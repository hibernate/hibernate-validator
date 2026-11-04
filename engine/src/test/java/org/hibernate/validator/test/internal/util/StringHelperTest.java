/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.Locale;

import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

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
		assertEquals( "", StringHelper.decapitalize( "" ) );
	}

	@Test
	public void decapitalizeShouldReturnLowerCaseLetter() {
		assertEquals( "a", StringHelper.decapitalize( "A" ) );
	}

	@Test
	public void decapitalizeShouldReturnDecapizalizedWord() {
		assertEquals( "giraffe", StringHelper.decapitalize( "Giraffe" ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1140")
	public void decapitalizeShouldReturnDecapizalizedWordOnTurkishLocale() {
		Locale defaultLocale = Locale.getDefault();
		Locale.setDefault( Locale.forLanguageTag( "tr-TR" ) );
		assertEquals( "isIsolationLevelGuaranteed", StringHelper.decapitalize( "IsIsolationLevelGuaranteed" ) );
		Locale.setDefault( defaultLocale );
	}

	@Test
	public void decapitalizeShouldReturnSameWordForDecapizalizedWord() {
		assertEquals( "giraffe", StringHelper.decapitalize( "giraffe" ) );
	}

	@Test
	public void decapitalizeShouldReturnSameWordForWordWithSeveralLeadingCapitalLetters() {
		assertEquals( "GIRaffe", StringHelper.decapitalize( "GIRaffe" ) );
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
