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

import java.io.File;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.ap.testmodel.FieldLevelValidationUsingBuiltInConstraints;
import org.hibernate.validator.ap.testmodel.MethodLevelValidationUsingBuiltInConstraints;
import org.hibernate.validator.ap.testmodel.boxing.ValidLong;
import org.hibernate.validator.ap.testmodel.boxing.ValidLongValidator;
import org.hibernate.validator.ap.testmodel.boxing.ValidationUsingBoxing;
import org.hibernate.validator.ap.testmodel.classlevelconstraints.ClassLevelValidation;
import org.hibernate.validator.ap.testmodel.classlevelconstraints.ValidCustomer;
import org.hibernate.validator.ap.testmodel.classlevelconstraints.ValidCustomerValidator;
import org.hibernate.validator.ap.testmodel.composedconstraint.FieldLevelValidationUsingComposedConstraint;
import org.hibernate.validator.ap.testmodel.composedconstraint.ValidOrderNumber;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposedConstraint;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint1;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint1ValidatorForGregorianCalendar;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint1ValidatorForList;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint1ValidatorForString;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint2;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint2ValidatorForArrayList;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint2ValidatorForCalendar;
import org.hibernate.validator.ap.testmodel.composedconstraint2.ComposingConstraint2ValidatorForCollection;
import org.hibernate.validator.ap.testmodel.composedconstraint2.FieldLevelValidationUsingComplexComposedConstraint;
import org.hibernate.validator.ap.testmodel.customconstraints.CaseMode;
import org.hibernate.validator.ap.testmodel.customconstraints.CheckCase;
import org.hibernate.validator.ap.testmodel.customconstraints.CheckCaseValidator;
import org.hibernate.validator.ap.testmodel.customconstraints.FieldLevelValidationUsingCustomConstraints;
import org.hibernate.validator.ap.testmodel.invalidcomposedconstraint.ValidCustomerNumber;
import org.hibernate.validator.ap.testutil.CompilerTestHelper;
import org.hibernate.validator.ap.util.DiagnosticExpection;

import static org.hibernate.validator.ap.testutil.CompilerTestHelper.assertDiagnostics;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;

/**
 * Test for {@link ConstraintValidationProcessor} using the Java compiler
 * API as defined by JSR 199.
 *
 * @author Gunnar Morling.
 */
public class ConstraintValidationProcessorTest {
	private static CompilerTestHelper compilerHelper;

	@BeforeClass
	public static void setUpCompilerHelper() {

		String testSourceBaseDir = System.getProperty( "testSourceBaseDir" );
		String pathToBeanValidationApiJar = System.getProperty( "pathToBeanValidationApiJar" );

		assertNotNull(
				testSourceBaseDir,
				"The system property testSourceBaseDir has to be set and point to the base directory of the test java sources."
		);
		assertNotNull(
				pathToBeanValidationApiJar,
				"The system property pathToBeanValidationApiJar has to be set and point to the BV API Jars."
		);

		compilerHelper =
				new CompilerTestHelper(
						ToolProvider.getSystemJavaCompiler(), testSourceBaseDir, pathToBeanValidationApiJar
				);
	}

	@Test
	public void fieldLevelValidationUsingBuiltInConstraints() {

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		File sourceFile = compilerHelper.getSourceFile( FieldLevelValidationUsingBuiltInConstraints.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertDiagnostics( diagnostics, new DiagnosticExpection( Kind.ERROR, 43 ) );
	}

	@Test
	public void fieldLevelValidationUsingCustomConstraints() {

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		File sourceFile1 = compilerHelper.getSourceFile( FieldLevelValidationUsingCustomConstraints.class );
		File sourceFile2 = compilerHelper.getSourceFile( CheckCase.class );
		File sourceFile3 = compilerHelper.getSourceFile( CaseMode.class );
		File sourceFile4 = compilerHelper.getSourceFile( CheckCaseValidator.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						sourceFile1,
						sourceFile2,
						sourceFile3,
						sourceFile4
				);

		assertFalse( compilationResult );
		assertDiagnostics( diagnostics, new DiagnosticExpection( Kind.ERROR, 30 ) );
	}

	@Test
	public void methodLevelValidationUsingBuiltInConstraints() {

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		File sourceFile = compilerHelper.getSourceFile( MethodLevelValidationUsingBuiltInConstraints.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertDiagnostics(
				diagnostics,
				new DiagnosticExpection( Kind.ERROR, 32 ),
				new DiagnosticExpection( Kind.ERROR, 39 ),
				new DiagnosticExpection( Kind.ERROR, 47 ),
				new DiagnosticExpection( Kind.ERROR, 54 )
		);
	}

	@Test
	public void classLevelValidation() {

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		File sourceFile1 = compilerHelper.getSourceFile( ClassLevelValidation.class );
		File sourceFile2 = compilerHelper.getSourceFile( ValidCustomer.class );
		File sourceFile3 = compilerHelper.getSourceFile( ValidCustomerValidator.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2, sourceFile3
				);

		assertFalse( compilationResult );
		assertDiagnostics( diagnostics, new DiagnosticExpection( Kind.ERROR, 28 ) );
	}

	@Test
	public void validationUsingComposedConstraint() {

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		File sourceFile1 = compilerHelper.getSourceFile( FieldLevelValidationUsingComposedConstraint.class );
		File sourceFile2 = compilerHelper.getSourceFile( ValidOrderNumber.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2 );

		assertFalse( compilationResult );
		assertDiagnostics( diagnostics, new DiagnosticExpection( Kind.ERROR, 29 ) );
	}

	@Test
	public void validationUsingComplexComposedConstraint() {

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		File sourceFile1 = compilerHelper.getSourceFile( FieldLevelValidationUsingComplexComposedConstraint.class );
		File sourceFile2 = compilerHelper.getSourceFile( ComposedConstraint.class );
		File sourceFile3 = compilerHelper.getSourceFile( ComposingConstraint1.class );
		File sourceFile4 = compilerHelper.getSourceFile( ComposingConstraint1ValidatorForString.class );
		File sourceFile5 = compilerHelper.getSourceFile( ComposingConstraint1ValidatorForGregorianCalendar.class );
		File sourceFile6 = compilerHelper.getSourceFile( ComposingConstraint1ValidatorForList.class );
		File sourceFile7 = compilerHelper.getSourceFile( ComposingConstraint2.class );
		File sourceFile8 = compilerHelper.getSourceFile( ComposingConstraint2ValidatorForArrayList.class );
		File sourceFile9 = compilerHelper.getSourceFile( ComposingConstraint2ValidatorForCalendar.class );
		File sourceFile10 = compilerHelper.getSourceFile( ComposingConstraint2ValidatorForCollection.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						sourceFile1,
						sourceFile2,
						sourceFile3,
						sourceFile4,
						sourceFile5,
						sourceFile6,
						sourceFile7,
						sourceFile8,
						sourceFile9,
						sourceFile10
				);

		assertFalse( compilationResult );
		assertDiagnostics(
				diagnostics,
				new DiagnosticExpection( Kind.ERROR, 29 ),
				new DiagnosticExpection( Kind.ERROR, 32 ),
				new DiagnosticExpection( Kind.ERROR, 41 ),
				new DiagnosticExpection( Kind.ERROR, 50 ),
				new DiagnosticExpection( Kind.ERROR, 56 )
		);
	}

	@Test
	public void validationUsingBoxing() {

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		File sourceFile1 = compilerHelper.getSourceFile( ValidationUsingBoxing.class );
		File sourceFile2 = compilerHelper.getSourceFile( ValidLong.class );
		File sourceFile3 = compilerHelper.getSourceFile( ValidLongValidator.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2, sourceFile3
				);

		assertFalse( compilationResult );
		assertDiagnostics(
				diagnostics,
				new DiagnosticExpection( Kind.ERROR, 31 ),
				new DiagnosticExpection( Kind.ERROR, 37 ),
				new DiagnosticExpection( Kind.ERROR, 43 ),
				new DiagnosticExpection( Kind.ERROR, 59 ),
				new DiagnosticExpection( Kind.ERROR, 67 )
		);
	}

	@Test
	public void constraintAnnotationGivenAtNonConstraintAnnotationType() {

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		File sourceFile = compilerHelper.getSourceFile( ValidCustomerNumber.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertDiagnostics(
				diagnostics, new DiagnosticExpection( Kind.ERROR, 28 ), new DiagnosticExpection( Kind.ERROR, 29 )
		);
	}
}