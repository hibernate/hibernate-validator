/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Date;

import org.hibernate.validator.internal.util.logging.formatter.ExecutableFormatter;
import org.hibernate.validator.test.internal.util.ExecutableHelperTest.Bar;
import org.hibernate.validator.test.internal.util.ExecutableHelperTest.Foo;

import org.junit.jupiter.api.Test;

/**
 * @author Gunnar Morling
 */
public class ExecutableFormatterTest {

	@Test
	public void executableAsStringShouldReturnMethodNameWithBracesForParameterlessMethod() throws Exception {
		assertEquals( "Foo#zap()", new ExecutableFormatter( Foo.class.getMethod( "zap" ) ).toString() );
		assertEquals( "Bar()", new ExecutableFormatter( Bar.class.getConstructor() ).toString() );
	}

	@Test
	public void executableAsStringShouldReturnMethodNameWithSimpleParameterTypeNames() throws Exception {
		assertEquals(
				"Bar#zap(int, Date)",
				new ExecutableFormatter( Bar.class.getMethod( "zap", int.class, Date.class ) ).toString() );
		assertEquals(
				"Bar(int, Date)",
				new ExecutableFormatter( Bar.class.getConstructor( int.class, Date.class ) ).toString() );
	}
}
