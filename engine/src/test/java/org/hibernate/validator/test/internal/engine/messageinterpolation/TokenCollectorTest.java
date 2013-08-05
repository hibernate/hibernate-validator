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
