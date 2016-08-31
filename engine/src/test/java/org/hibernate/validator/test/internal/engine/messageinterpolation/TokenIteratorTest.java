/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenCollector;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenIterator;
import org.testng.annotations.Test;

/**
 * Tests for {@code TokenIterator}.
 *
 * @author Hardy Ferentschik
 */
public class TokenIteratorTest {

	@Test(expectedExceptions = IllegalStateException.class)
	public void testGettingInterpolatedMessageWithoutCallingHasMoreInterpolationTerms() throws Exception {
		TokenCollector tokenCollector = new TokenCollector( "foo", InterpolationTermType.PARAMETER );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );
		tokenIterator.getInterpolatedMessage();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testNextInterpolationTermWithoutCallingHasMoreInterpolationTerms() throws Exception {
		TokenCollector tokenCollector = new TokenCollector( "foo", InterpolationTermType.PARAMETER );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );
		tokenIterator.nextInterpolationTerm();
	}

	@Test
	public void testMessageDescriptorWithoutParameter() throws Exception {
		String message = "this message has no parameter";
		TokenCollector tokenCollector = new TokenCollector( message, InterpolationTermType.PARAMETER );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );

		assertFalse( tokenIterator.hasMoreInterpolationTerms(), "There should be no interpolation terms" );
		assertEquals( tokenIterator.getInterpolatedMessage(), message, "The message should be unchanged" );
	}

	@Test
	public void testParameterTermHasPrecedenceForParameterParser() throws Exception {
		TokenCollector tokenCollector = new TokenCollector( "${foo}", InterpolationTermType.PARAMETER );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );
		assertSingleReplacement( tokenIterator, "{foo}", "bar", "$bar" );
	}

	@Test
	public void testFindParameterTerms() throws Exception {
		String message = "{foo} {bar}";
		TokenCollector tokenCollector = new TokenCollector( message, InterpolationTermType.PARAMETER );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );

		assertTrue( tokenIterator.hasMoreInterpolationTerms(), "There should be a term" );
		assertEquals( tokenIterator.nextInterpolationTerm(), "{foo}", "{foo} should be the first term" );

		assertTrue( tokenIterator.hasMoreInterpolationTerms(), "There should be a term" );
		assertEquals( tokenIterator.nextInterpolationTerm(), "{bar}", "{bar} should be the second term" );

		assertFalse( tokenIterator.hasMoreInterpolationTerms(), "There should be no more interpolation terms" );
	}

	@Test
	public void testEscapedMetaCharactersStayUntouched() throws Exception {
		String message = "\\} \\{ \\$ \\\\";
		TokenCollector tokenCollector = new TokenCollector( message, InterpolationTermType.PARAMETER );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );

		assertFalse( tokenIterator.hasMoreInterpolationTerms(), "There should be no term" );
		assertEquals(
				tokenIterator.getInterpolatedMessage(),
				message,
				"Message should not change since all meta characters are escaped"
		);
	}

	@Test
	public void testUnEscapedExpressionLanguageLiteral() throws Exception {
		TokenCollector tokenCollector = new TokenCollector(
				"The price is US$ {value}",
				InterpolationTermType.PARAMETER
		);
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );

		assertSingleReplacement( tokenIterator, "{value}", "100", "The price is US$ 100" );
	}

	@Test
	public void testEscapedExpressionLanguageLiteralParameterParsing() throws Exception {
		TokenCollector tokenCollector = new TokenCollector(
				"The price is US\\$ {value}",
				InterpolationTermType.PARAMETER
		);
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );

		assertSingleReplacement( tokenIterator, "{value}", "100", "The price is US\\$ 100" );
	}

	@Test
	public void testExpressionLanguageLiteralParameterParsing() throws Exception {
		TokenCollector tokenCollector = new TokenCollector(
				"The price is US$ {value}",
				InterpolationTermType.PARAMETER
		);
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );

		assertSingleReplacement( tokenIterator, "{value}", "100", "The price is US$ 100" );
	}

	@Test
	public void testExpressionLanguageLiteralELParsing() throws Exception {
		String message = "The price is US$ {value}";
		TokenCollector tokenCollector = new TokenCollector( message, InterpolationTermType.EL );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );

		assertFalse( tokenIterator.hasMoreInterpolationTerms(), "There should be no interpolation terms" );
		assertEquals( tokenIterator.getInterpolatedMessage(), message, "The message should be unchanged" );
	}

	@Test
	public void testReplaceParameter() throws Exception {
		TokenCollector tokenCollector = new TokenCollector( "{foo}", InterpolationTermType.PARAMETER );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );

		assertSingleReplacement( tokenIterator, "{foo}", "bar", "bar" );
	}

	@Test
	public void testReplaceParameterInline() throws Exception {
		TokenCollector tokenCollector = new TokenCollector( "a{var}c", InterpolationTermType.PARAMETER );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );

		assertSingleReplacement( tokenIterator, "{var}", "b", "abc" );
	}

	@Test
	public void testReplaceParameterInEscapedBraces() throws Exception {
		TokenCollector tokenCollector = new TokenCollector( "\\{{var}\\}", InterpolationTermType.PARAMETER );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );

		assertSingleReplacement( tokenIterator, "{var}", "foo", "\\{foo\\}" );
	}

	@Test
	public void testELParameter() throws Exception {
		TokenCollector tokenCollector = new TokenCollector( "${foo}", InterpolationTermType.EL );
		TokenIterator tokenIterator = new TokenIterator( tokenCollector.getTokenList() );

		assertSingleReplacement( tokenIterator, "${foo}", "bar", "bar" );
	}

	private void assertSingleReplacement(TokenIterator tokenIterator,
			String term,
			String termReplacement,
			String interpolatedMessage)
			throws Exception {
		assertTrue( tokenIterator.hasMoreInterpolationTerms(), "There should be a term" );

		String actualTerm = tokenIterator.nextInterpolationTerm();
		assertEquals( actualTerm, term, "Wrong term" );
		tokenIterator.replaceCurrentInterpolationTerm( termReplacement );

		assertFalse( tokenIterator.hasMoreInterpolationTerms(), "There should be no more term" );
		assertEquals(
				tokenIterator.getInterpolatedMessage(),
				interpolatedMessage,
				"Term did not get properly replaced"
		);
	}
}
