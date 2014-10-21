/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.messageinterpolation;

import org.hibernate.validator.internal.engine.messageinterpolation.InterpolationTermType;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.MessageDescriptorFormatException;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.TokenCollector;
import org.testng.annotations.Test;

/**
 * Tests for {@code TokenCollector}.
 *
 * @author Hardy Ferentschik
 */
public class TokenCollectorTest {

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000169.*")
	public void testNestedParametersThrowException() throws Exception {
		new TokenCollector( "#{foo  {}", InterpolationTermType.PARAMETER );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testParameterWithoutOpeningBraceThrowsException() throws Exception {
		new TokenCollector( "foo}", InterpolationTermType.PARAMETER );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testELExpressionWithoutOpeningBraceThrowsException() throws Exception {
		new TokenCollector( "$}", InterpolationTermType.EL );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testTermWithoutClosingBraceThrowsException() throws Exception {
		new TokenCollector( "{foo", InterpolationTermType.PARAMETER );
	}

	@Test(expectedExceptions = MessageDescriptorFormatException.class, expectedExceptionsMessageRegExp = "HV000168.*")
	public void testSingleClosingBraceThrowsException() throws Exception {
		new TokenCollector( "this message contains a invalid parameter start token {", InterpolationTermType.EL );
	}
}
