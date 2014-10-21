/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.hibernate.validator.ap.testutil.CompilerTestHelper;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Base class providing common functionality for all tests for the constraint validation processor using the Java compiler
 * API as defined by JSR 199.
 *
 * @author Gunnar Morling
 */
public abstract class ConstraintValidationProcessorTestBase {

	protected static CompilerTestHelper compilerHelper;

	protected DiagnosticCollector<JavaFileObject> diagnostics;

	@BeforeClass
	public static void setUpCompilerHelper() {

		compilerHelper =
				new CompilerTestHelper( ToolProvider.getSystemJavaCompiler() );
	}

	@BeforeMethod
	public void setUpDiagnostics() {
		diagnostics = new DiagnosticCollector<JavaFileObject>();
	}

}
