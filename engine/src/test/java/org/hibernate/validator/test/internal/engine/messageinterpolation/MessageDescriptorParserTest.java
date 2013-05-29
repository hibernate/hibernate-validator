/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,  
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorParser;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;


/**
 * Tests for {@code MessageDescriptorParser}.
 *
 * @author Hardy Ferentschik
 */
public class MessageDescriptorParserTest {

	@Test(expectedExceptions = IllegalStateException.class)
	public void testGettingInterpolatedMessageWithoutCallingHasMoreInterpolationTerms() throws Exception {
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( "foo" );
		descriptorParser.getInterpolatedMessage();
	}

	@Test(expectedExceptions = IllegalStateException.class)
	public void testNextInterpolationTermWithoutCallingHasMoreInterpolationTerms() throws Exception {
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( "foo" );
		descriptorParser.nextInterpolationTerm();
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000169.*")
	public void testNestedParametersThrowException() throws Exception {
		MessageDescriptorParser.forParameter( "#{foo  {}" );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class)
	public void testTermWithoutOpeningBraceThrowsException() throws Exception {
		MessageDescriptorParser.forParameter( "foo}" );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testParameterWithoutOpeningBraceThrowsException() throws Exception {
		MessageDescriptorParser.forParameter( "foo}" );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testELExpressionWithoutOpeningBraceThrowsException() throws Exception {
		MessageDescriptorParser.forExpressionLanguage( "$}" );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testTermWithoutClosingBraceThrowsException() throws Exception {
		MessageDescriptorParser.forParameter( "{foo" );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testSingleClosingBraceThrowsException() throws Exception {
		MessageDescriptorParser.forParameter(
				"this message contains a invalid parameter start token {"
		);
	}

	@Test
	public void testMessageDescriptorWithoutParameter() throws Exception {
		String message = "this message has no parameter";
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( message );

		assertFalse( descriptorParser.hasMoreInterpolationTerms(), "There should be no interpolation terms" );
		assertEquals( descriptorParser.getInterpolatedMessage(), message, "The message should be unchanged" );
	}

	@Test
	public void testParameterTermHasPrecedenceForParameterParser() throws Exception {
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( "${foo}" );
		assertSingleReplacement( descriptorParser, "{foo}", "bar", "$bar" );
	}

	@Test
	public void testFindParameterTerms() throws Exception {
		String message = "{foo} {bar}";
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( message );

		assertTrue( descriptorParser.hasMoreInterpolationTerms(), "The should be term" );
		assertEquals( descriptorParser.nextInterpolationTerm(), "{foo}", "{foo} should be the first term" );

		assertTrue( descriptorParser.hasMoreInterpolationTerms(), "The should be term" );
		assertEquals( descriptorParser.nextInterpolationTerm(), "{bar}", "{bar} should be the second term" );

		assertFalse( descriptorParser.hasMoreInterpolationTerms(), "There should be no more interpolation terms" );
	}

	@Test
	public void testEscapedMetaCharactersStayUntouched() throws Exception {
		String message = "\\} \\{ \\$ \\\\";
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( message );

		assertFalse( descriptorParser.hasMoreInterpolationTerms(), "The should be no term" );
		assertEquals(
				descriptorParser.getInterpolatedMessage(),
				message,
				"Message should not change since all meta characters are escaped"
		);
	}

	@Test
	public void testUnEscapedExpressionLanguageLiteral() throws Exception {
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( "The price is US$ {value}" );
		assertSingleReplacement( descriptorParser, "{value}", "100", "The price is US$ 100" );
	}

	@Test
	public void testEscapedExpressionLanguageLiteralParameterParsing() throws Exception {
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( "The price is US\\$ {value}" );
		assertSingleReplacement( descriptorParser, "{value}", "100", "The price is US\\$ 100" );
	}

	@Test
	public void testExpressionLanguageLiteralParameterParsing() throws Exception {
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( "The price is US$ {value}" );
		assertSingleReplacement( descriptorParser, "{value}", "100", "The price is US$ 100" );
	}

	@Test
	public void testExpressionLanguageLiteralELParsing() throws Exception {
		String message = "The price is US$ {value}";
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forExpressionLanguage( message );

		assertFalse( descriptorParser.hasMoreInterpolationTerms(), "There should be no interpolation terms" );
		assertEquals( descriptorParser.getInterpolatedMessage(), message, "The message should be unchanged" );
	}

	@Test
	public void testReplaceParameter() throws Exception {
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( "{foo}" );
		assertSingleReplacement( descriptorParser, "{foo}", "bar", "bar" );
	}

	@Test
	public void testReplaceParameterInline() throws Exception {
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( "a{var}c" );
		assertSingleReplacement( descriptorParser, "{var}", "b", "abc" );
	}

	@Test
	public void testReplaceParameterInEscapedBraces() throws Exception {
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forParameter( "\\{{var}\\}" );
		assertSingleReplacement( descriptorParser, "{var}", "foo", "\\{foo\\}" );
	}

	@Test
	public void testELParameter() throws Exception {
		MessageDescriptorParser descriptorParser = MessageDescriptorParser.forExpressionLanguage( "${foo}" );
		assertSingleReplacement( descriptorParser, "${foo}", "bar", "bar" );
	}

	private void assertSingleReplacement(MessageDescriptorParser descriptorParser,
										 String term,
										 String termReplacement,
										 String interpolatedMessage)
			throws Exception {
		assertTrue( descriptorParser.hasMoreInterpolationTerms(), "The should be a term" );

		String actualTerm = descriptorParser.nextInterpolationTerm();
		assertEquals( actualTerm, term, "Wrong term" );
		descriptorParser.replaceCurrentInterpolationTerm( termReplacement );

		assertFalse( descriptorParser.hasMoreInterpolationTerms(), "The should be no more term" );
		assertEquals(
				descriptorParser.getInterpolatedMessage(),
				interpolatedMessage,
				"Term did not get properly replaced"
		);
	}
}
