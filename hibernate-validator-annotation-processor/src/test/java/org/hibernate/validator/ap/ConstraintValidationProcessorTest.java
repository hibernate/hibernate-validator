// $Id: ConstraintValidationProcessorTest.java 17946 2009-11-06 18:23:48Z hardy.ferentschik $
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

import static org.hibernate.validator.ap.testutil.CompilerTestHelper.assertDiagnostics;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

import java.io.File;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.Diagnostic.Kind;

import org.hibernate.validator.ap.testmodel.FieldLevelValidationUsingBuiltInConstraints;
import org.hibernate.validator.ap.testmodel.MethodLevelValidationUsingBuiltInConstraints;
import org.hibernate.validator.ap.testmodel.customconstraints.CaseMode;
import org.hibernate.validator.ap.testmodel.customconstraints.CheckCase;
import org.hibernate.validator.ap.testmodel.customconstraints.CheckCaseValidator;
import org.hibernate.validator.ap.testmodel.customconstraints.FieldLevelValidationUsingCustomConstraints;
import org.hibernate.validator.ap.testutil.CompilerTestHelper;
import org.hibernate.validator.ap.util.DiagnosticExpection;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test for {@link ConstraintValidationProcessor} using the Java compiler
 * API as defined by JSR 199.
 * 
 * @author Gunnar Morling.
 *
 */
public class ConstraintValidationProcessorTest {

	private static CompilerTestHelper compilerHelper;
	
	@BeforeClass
	public static void setUpCompilerHelper() {

		String testSourceBaseDir = System.getProperty("testSourceBaseDir");
		String pathToBeanValidationApiJar = System.getProperty("pathToBeanValidationApiJar");
		
		assertNotNull(testSourceBaseDir, "The system property testSourceBaseDir has to be set and point to the base directory of the test java sources.");
		assertNotNull(pathToBeanValidationApiJar, "The system property pathToBeanValidationApiJar has to be set and point to the BV API Jars.");
		
		compilerHelper = 
			new CompilerTestHelper(ToolProvider.getSystemJavaCompiler(), testSourceBaseDir, pathToBeanValidationApiJar);
	}

	@Test
	public void fieldLevelValidationUsingBuiltInConstraints() {

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		
		File sourceFile = compilerHelper.getSourceFile(FieldLevelValidationUsingBuiltInConstraints.class);
		
		boolean compilationResult = 
			compilerHelper.compile(new ConstraintValidationProcessor(), diagnostics, sourceFile);

		assertFalse(compilationResult);
		assertDiagnostics(diagnostics, new DiagnosticExpection(Kind.ERROR, 48));
	}
	
	@Test
	public void fieldLevelValidationUsingCustomConstraints() {

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		
		File sourceFile1 = compilerHelper.getSourceFile(FieldLevelValidationUsingCustomConstraints.class);
		File sourceFile2 = compilerHelper.getSourceFile(CheckCase.class);
		File sourceFile3 = compilerHelper.getSourceFile(CaseMode.class);
		File sourceFile4 = compilerHelper.getSourceFile(CheckCaseValidator.class);
		
		boolean compilationResult = 
			compilerHelper.compile(new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2, sourceFile3, sourceFile4);

		assertFalse(compilationResult);
		assertDiagnostics(diagnostics, new DiagnosticExpection(Kind.ERROR, 30));
	}
	
	@Test
	public void methodLevelValidationUsingBuiltInConstraints() {

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		
		File sourceFile = compilerHelper.getSourceFile(MethodLevelValidationUsingBuiltInConstraints.class);
		
		boolean compilationResult = 
			compilerHelper.compile(new ConstraintValidationProcessor(), diagnostics, sourceFile);

		assertFalse(compilationResult);
		assertDiagnostics(diagnostics, new DiagnosticExpection(Kind.ERROR, 32));
	}
	
}