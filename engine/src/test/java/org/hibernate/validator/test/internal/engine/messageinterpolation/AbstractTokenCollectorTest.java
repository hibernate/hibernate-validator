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
import org.testng.annotations.Test;

/**
 * Abstract base for {@code TokenCollector} tests.
 *
 * @author Hardy Ferentschik
 */
public abstract class AbstractTokenCollectorTest {

	protected abstract InterpolationTermType getInterpolationTermType();

	@Test
	public void testLiteral() {
		Assertions.assertThat(
				new TokenCollector( "foo bar", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 )
				.element( 0 )
				.returns( "foo bar", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000169.*")
	public void testNestedParametersThrowException() {
		new TokenCollector( "#{foo  {}", getInterpolationTermType() );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testClosingBraceWithoutOpeningBraceThrowsException() {
		new TokenCollector( "foo}", getInterpolationTermType() );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testOpeningBraceWithoutClosingBraceThrowsException() {
		new TokenCollector( "{foo", getInterpolationTermType() );
	}

	@Test
	public void testBackslashEscapesNonMetaCharacter() {
		Assertions.assertThat(
				new TokenCollector( "foo \\bar", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 )
				.element( 0 )
				// Backslashes are removed later, in AbstractMessageInterpolator.replaceEscapedLiterals
				.returns( "foo \\bar", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test
	public void testBackslashEscapesDollar() {
		Assertions.assertThat(
				new TokenCollector( "foo \\$ bar", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 )
				.element( 0 )
				// Backslashes are removed later, in AbstractMessageInterpolator.replaceEscapedLiterals
				.returns( "foo \\$ bar", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test
	public void testBackslashEscapesOpeningBrace() {
		Assertions.assertThat(
				new TokenCollector( "foo \\{ bar", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 )
				.element( 0 )
				// Backslashes are removed later, in AbstractMessageInterpolator.replaceEscapedLiterals
				.returns( "foo \\{ bar", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test
	public void testBackslashEscapesClosingBrace() {
		Assertions.assertThat(
				new TokenCollector( "foo \\} bar", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 )
				.element( 0 )
				// Backslashes are removed later, in AbstractMessageInterpolator.replaceEscapedLiterals
				.returns( "foo \\} bar", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test
	public void testBackslashEscapesBackslash() {
		Assertions.assertThat(
				new TokenCollector( "foo \\\\ bar", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 )
				.element( 0 )
				// Backslashes are removed later, in AbstractMessageInterpolator.replaceEscapedLiterals
				.returns( "foo \\\\ bar", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test
	public void testBackslashEscapesEL() {
		Assertions.assertThat(
				new TokenCollector( "foo \\$\\{bar\\}", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 )
				.element( 0 )
				// Backslashes are removed later, in AbstractMessageInterpolator.replaceEscapedLiterals
				.returns( "foo \\$\\{bar\\}", Token::getTokenValue )
				// What's important is that we did NOT detect the expression
				.returns( false, Token::isParameter );
	}

	@Test
	public void testBackslashEscapesParameter() {
		Assertions.assertThat(
				new TokenCollector( "foo \\{bar\\}", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 )
				.element( 0 )
				// Backslashes are removed later, in AbstractMessageInterpolator.replaceEscapedLiterals
				.returns( "foo \\{bar\\}", Token::getTokenValue )
				// What's important is that we did NOT detect the parameter
				.returns( false, Token::isParameter );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testTrailingClosingBraceThrowsException() {
		new TokenCollector( "this message contains a invalid parameter start token {", getInterpolationTermType() );
	}

	@Test
	public void testDollarThenNonMetaCharacterInterpretedAsLiteral() {
		Assertions.assertThat(
				new TokenCollector( "$a", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 )
				.element( 0 )
				.returns( "$a", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test
	public void testTrailingDollarInterpretedAsLiteral() {
		Assertions.assertThat(
				new TokenCollector( "foo $", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 )
				.element( 0 )
				.returns( "foo $", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}

	@Test
	public void testTrailingBackslashInterpretedAsLiteral() {
		Assertions.assertThat(
				new TokenCollector( "foo \\", getInterpolationTermType() )
						.getTokenList()
		)
				.hasSize( 1 )
				.element( 0 )
				.returns( "foo \\", Token::getTokenValue )
				.returns( false, Token::isParameter );
	}
}
