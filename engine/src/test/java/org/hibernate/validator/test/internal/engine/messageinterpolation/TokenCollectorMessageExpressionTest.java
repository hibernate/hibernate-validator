/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.Token;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenCollector;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ListAssert;
import org.testng.annotations.Test;

/**
 * Tests for {@code TokenCollector} in message expression mode.
 *
 * @author Hardy Ferentschik
 */
public class TokenCollectorMessageExpressionTest extends AbstractTokenCollectorTest {
	@Override
	protected InterpolationTermType getInterpolationTermType() {
		return InterpolationTermType.EL;
	}

	// Several tests inherited from the abstract class

	@Test
	public void testMessageParameter() {
		ListAssert<Token> assertion = Assertions.assertThat(
				new TokenCollector( "foo {bar}", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 2 );
		assertion.element( 0 )
				.returns( "foo ", Token::getTokenValue )
				.returns( false, Token::isParameter );
		assertion.element( 1 )
				.returns( "{bar}", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test
	public void testMessageExpression() {
		ListAssert<Token> assertion = Assertions.assertThat(
				new TokenCollector( "foo ${bar}", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 2 );
		assertion.element( 0 )
				.returns( "foo ", Token::getTokenValue )
				.returns( false, Token::isParameter );
		assertion.element( 1 )
				.returns( "${bar}", Token::getTokenValue )
				.returns( true, Token::isParameter );
	}

	@Test
	public void testDollarThenDollarThenParameterInterpretedAsLiterals() {
		ListAssert<Token> assertion = Assertions.assertThat(
				new TokenCollector( "$${1+1}", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 2 );
		assertion.element( 0 )
				.returns( "$$", Token::getTokenValue )
				.returns( false, Token::isParameter );
		assertion.element( 1 )
				.returns( "{1+1}", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test
	public void testDollarThenDollarThenLiteralsInterpretedAsLiterals() {
		ListAssert<Token> assertion = Assertions.assertThat(
				new TokenCollector( "$$foo", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 2 );
		assertion.element( 0 )
				.returns( "$$", Token::getTokenValue )
				.returns( false, Token::isParameter );
		assertion.element( 1 )
				.returns( "foo", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testDollarThenClosingBraceThrowsException() {
		new TokenCollector( "$}", getInterpolationTermType() );
	}

	@Test
	public void testDollarThenEscapeInterpretedAsLiterals() {
		ListAssert<Token> assertion = Assertions.assertThat(
				new TokenCollector( "$\\A{1+1}", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 2 );
		assertion.element( 0 )
				.returns( "$\\A", Token::getTokenValue )
				.returns( false, Token::isParameter );
		assertion.element( 1 )
				.returns( "{1+1}", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}
}
