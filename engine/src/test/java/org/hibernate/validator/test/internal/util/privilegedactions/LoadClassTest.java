/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util.privilegedactions;

import jakarta.validation.ValidationException;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.util.privilegedactions.LoadClass;

import static java.lang.Thread.currentThread;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.testng.Assert.assertTrue;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class LoadClassTest {
	@Test
	public void test_loading_dummy_class_throws_exception_without_fallback_to_tcl() {
		final LoadClass action = LoadClass.action( "org.hibernate.validator.Dummy", null, false );
		runLoadClass( action );
	}

	@Test
	public void test_loading_dummy_class_throws_exception_with_fallback_to_tcl() {
		final LoadClass action = LoadClass.action( "org.hibernate.validator.Dummy", null, true );
		final ClassLoader current = currentThread().getContextClassLoader();
		try {
			currentThread().setContextClassLoader( null );
			runLoadClass( action );
		}
		finally {
			currentThread().setContextClassLoader( current );
		}
	}

	private void runLoadClass(LoadClass action) {
		try {
			action.run();
			fail( "Should have thrown jakarta.validation.ValidationException" );
		}
		catch (ValidationException e) {
			String expectedMessageId = "HV000065";
			assertTrue(
					e.getMessage().startsWith( expectedMessageId ),
					"Wrong error message. Expected " + expectedMessageId + " ,but got " + e.getMessage()
			);
			assertNotNull( e.getCause(), "HV-1026: exception cause should be set" );
		}
	}
}
