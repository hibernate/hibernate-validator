/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util;

import static org.testng.Assert.assertEquals;

import java.util.Date;

import org.hibernate.validator.internal.util.logging.formatter.ExecutableFormatter;
import org.hibernate.validator.test.internal.util.ExecutableHelperTest.Bar;
import org.hibernate.validator.test.internal.util.ExecutableHelperTest.Foo;
import org.testng.annotations.Test;

/**
 * @author Gunnar Morling
 */
public class ExecutableFormatterTest {

	@Test
	public void executableAsStringShouldReturnMethodNameWithBracesForParameterlessMethod() throws Exception {
		assertEquals( new ExecutableFormatter( Foo.class.getMethod( "zap" ) ).toString(), "Foo#zap()" );
		assertEquals( new ExecutableFormatter( Bar.class.getConstructor() ).toString(), "Bar()" );
	}

	@Test
	public void executableAsStringShouldReturnMethodNameWithSimpleParameterTypeNames() throws Exception {
		assertEquals(
				new ExecutableFormatter( Bar.class.getMethod( "zap", int.class, Date.class ) ).toString(),
				"Bar#zap(int, Date)"
		);
		assertEquals(
				new ExecutableFormatter( Bar.class.getConstructor( int.class, Date.class ) ).toString(),
				"Bar(int, Date)"
		);
	}
}
