/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
 * Tests for {@code TokenCollector} in message parameter mode.
 *
 * @author Hardy Ferentschik
 */
public class TokenCollectorMessageParameterTest extends AbstractTokenCollectorTest {
	@Override
	protected InterpolationTermType getInterpolationTermType() {
		return InterpolationTermType.PARAMETER;
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
				.returns( true, Token::isParameter );
	}

	@Test
	public void testMessageExpression() {
		ListAssert<Token> assertion = Assertions.assertThat(
				new TokenCollector( "foo ${bar}", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 2 );
		/*
		 * 6.3.1.1:
		 * Parameter interpolation has precedence over message expressions.
		 * For example for the message descriptor ${value},
		 * trying to evaluate {value} as message parameter has precedence
		 * over evaluating ${value} as message expression.
		 */
		assertion.element( 0 )
				.returns( "foo $", Token::getTokenValue )
				.returns( false, Token::isParameter );
		assertion.element( 1 )
				.returns( "{bar}", Token::getTokenValue )
				.returns( true, Token::isParameter );
	}

	@Test
	public void testDollarThenDollarThenParameterInterpretedAsLiteralAndParameter() {
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
				.returns( true, Token::isParameter );
	}

	@Test
	public void testDollarThenDollarThenLiteralsInterpretedAsLiterals() {
		ListAssert<Token> assertion = Assertions.assertThat(
				new TokenCollector( "$$foo", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 );
		assertion.element( 0 )
				.returns( "$$foo", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testDollarThenClosingBraceThrowsException() {
		// Fails because of the dangling closing brace; the dollar sign is irrelevant
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
				.returns( true, Token::isParameter );
	}
}
