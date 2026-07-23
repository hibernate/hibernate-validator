/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

	@Test
	public void testNestedParametersThrowException() {
		assertThatThrownBy( () -> new TokenCollector( "#{foo  {}", getInterpolationTermType() ) )
				.isInstanceOf( MessageDescriptorFormatException.class )
				.hasMessageMatching( "HV000169.*" );
	}

	@Test
	public void testClosingBraceWithoutOpeningBraceThrowsException() {
		assertThatThrownBy( () -> new TokenCollector( "foo}", getInterpolationTermType() ) )
				.isInstanceOf( MessageDescriptorFormatException.class )
				.hasMessageMatching( "HV000168.*" );
	}

	@Test
	public void testOpeningBraceWithoutClosingBraceThrowsException() {
		assertThatThrownBy( () -> new TokenCollector( "{foo", getInterpolationTermType() ) )
				.isInstanceOf( MessageDescriptorFormatException.class )
				.hasMessageMatching( "HV000168.*" );
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

	@Test
	public void testTrailingClosingBraceThrowsException() {
		assertThatThrownBy( () -> new TokenCollector( "this message contains a invalid parameter start token {", getInterpolationTermType() ) )
				.isInstanceOf( MessageDescriptorFormatException.class )
				.hasMessageMatching( "HV000168.*" );
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
