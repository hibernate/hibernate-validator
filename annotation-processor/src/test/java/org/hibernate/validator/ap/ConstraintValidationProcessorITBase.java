/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.hibernate.validator.ap.testutil.CompilerTestHelper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class providing common functionality for all tests for the constraint validation processor using the Java compiler
 * API as defined by JSR 199.
 *
 * @author Gunnar Morling
 */
public abstract class ConstraintValidationProcessorITBase {

	protected static CompilerTestHelper compilerHelper;

	protected DiagnosticCollector<JavaFileObject> diagnostics;

	@BeforeAll
	public static void setUpCompilerHelper() {

		compilerHelper =
				new CompilerTestHelper( ToolProvider.getSystemJavaCompiler() );
	}

	@BeforeEach
	public void setUpDiagnostics() {
		diagnostics = new DiagnosticCollector<JavaFileObject>();
	}

}
