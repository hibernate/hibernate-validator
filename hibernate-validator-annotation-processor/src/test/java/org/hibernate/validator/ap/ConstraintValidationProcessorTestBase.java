/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
