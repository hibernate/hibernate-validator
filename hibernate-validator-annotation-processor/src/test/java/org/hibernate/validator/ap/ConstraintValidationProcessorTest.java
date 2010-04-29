// $Id$
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.ap.testmodel.FieldLevelValidationUsingBuiltInConstraints;
import org.hibernate.validator.ap.testmodel.MethodLevelValidationUsingBuiltInConstraints;
import org.hibernate.validator.ap.testmodel.MultipleConstraintsOfSameType;
import org.hibernate.validator.ap.testmodel.ValidationUsingAtValidAnnotation;
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
import org.hibernate.validator.ap.testmodel.inheritedvalidator.AbstractCustomConstraintValidator;
import org.hibernate.validator.ap.testmodel.inheritedvalidator.CustomConstraint;
import org.hibernate.validator.ap.testmodel.inheritedvalidator.CustomConstraintValidator;
import org.hibernate.validator.ap.testmodel.inheritedvalidator.FieldLevelValidationUsingInheritedValidator;
import org.hibernate.validator.ap.testmodel.invalidcomposedconstraint.ValidCustomerNumber;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.NoUniqueValidatorResolution;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.SerializableCollection;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.Size;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.SizeValidatorForCollection;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.SizeValidatorForSerializable;
import org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution.SizeValidatorForSet;
import org.hibernate.validator.ap.testutil.CompilerTestHelper;
import org.hibernate.validator.ap.util.DiagnosticExpection;

import static org.hibernate.validator.ap.testutil.CompilerTestHelper.assertThatDiagnosticsMatch;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Test for {@link ConstraintValidationProcessor} using the Java compiler
 * API as defined by JSR 199.
 *
 * @author Gunnar Morling.
 */
public class ConstraintValidationProcessorTest {

	private static CompilerTestHelper compilerHelper;

	private DiagnosticCollector<JavaFileObject> diagnostics;

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

	@BeforeMethod
	public void setUp() {
		diagnostics = new DiagnosticCollector<JavaFileObject>();
	}

	@Test
	public void fieldLevelValidationUsingBuiltInConstraints() {

		File sourceFile = compilerHelper.getSourceFile( FieldLevelValidationUsingBuiltInConstraints.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics, new DiagnosticExpection( Kind.ERROR, 54 ), new DiagnosticExpection( Kind.ERROR, 60 )
		);
	}

	@Test
	public void compilationSucceedsDueToDiagnosticKindWarning() {

		File sourceFile = compilerHelper.getSourceFile( FieldLevelValidationUsingBuiltInConstraints.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, Kind.WARNING, sourceFile );

		assertTrue( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics, new DiagnosticExpection( Kind.WARNING, 54 ), new DiagnosticExpection( Kind.WARNING, 60 )
		);
	}

	@Test
	public void fieldLevelValidationUsingCustomConstraints() {

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
		assertThatDiagnosticsMatch( diagnostics, new DiagnosticExpection( Kind.ERROR, 30 ) );
	}

	@Test
	public void testThatInheritedValidatorClassesAreHandledCorrectly() {

		File sourceFile1 = compilerHelper.getSourceFile( FieldLevelValidationUsingInheritedValidator.class );
		File sourceFile2 = compilerHelper.getSourceFile( CustomConstraint.class );
		File sourceFile3 = compilerHelper.getSourceFile( AbstractCustomConstraintValidator.class );
		File sourceFile4 = compilerHelper.getSourceFile( CustomConstraintValidator.class );

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
		assertThatDiagnosticsMatch( diagnostics, new DiagnosticExpection( Kind.ERROR, 30 ) );
	}

	@Test
	public void methodLevelValidationUsingBuiltInConstraints() {

		File sourceFile = compilerHelper.getSourceFile( MethodLevelValidationUsingBuiltInConstraints.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpection( Kind.ERROR, 32 ),
				new DiagnosticExpection( Kind.ERROR, 39 ),
				new DiagnosticExpection( Kind.ERROR, 47 ),
				new DiagnosticExpection( Kind.ERROR, 54 )
		);
	}

	@Test
	public void classLevelValidation() {

		File sourceFile1 = compilerHelper.getSourceFile( ClassLevelValidation.class );
		File sourceFile2 = compilerHelper.getSourceFile( ValidCustomer.class );
		File sourceFile3 = compilerHelper.getSourceFile( ValidCustomerValidator.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2, sourceFile3
				);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch( diagnostics, new DiagnosticExpection( Kind.ERROR, 28 ) );
	}

	@Test
	public void validationUsingComposedConstraint() {

		File sourceFile1 = compilerHelper.getSourceFile( FieldLevelValidationUsingComposedConstraint.class );
		File sourceFile2 = compilerHelper.getSourceFile( ValidOrderNumber.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2 );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch( diagnostics, new DiagnosticExpection( Kind.ERROR, 29 ) );
	}

	@Test
	public void validationUsingComplexComposedConstraint() {

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
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpection( Kind.ERROR, 29 ),
				new DiagnosticExpection( Kind.ERROR, 41 ),
				new DiagnosticExpection( Kind.ERROR, 50 ),
				new DiagnosticExpection( Kind.ERROR, 56 )
		);
	}

	@Test
	public void validationUsingBoxing() {

		File sourceFile1 = compilerHelper.getSourceFile( ValidationUsingBoxing.class );
		File sourceFile2 = compilerHelper.getSourceFile( ValidLong.class );
		File sourceFile3 = compilerHelper.getSourceFile( ValidLongValidator.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2, sourceFile3
				);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpection( Kind.ERROR, 31 ),
				new DiagnosticExpection( Kind.ERROR, 37 ),
				new DiagnosticExpection( Kind.ERROR, 43 ),
				new DiagnosticExpection( Kind.ERROR, 59 ),
				new DiagnosticExpection( Kind.ERROR, 67 )
		);
	}

	@Test
	public void testThatSpecifyingConstraintAnnotationAtNonConstraintAnnotationTypeCausesCompilationError() {

		File sourceFile = compilerHelper.getSourceFile( ValidCustomerNumber.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics, new DiagnosticExpection( Kind.ERROR, 28 ), new DiagnosticExpection( Kind.ERROR, 29 )
		);
	}

	@Test
	public void testThatNonUniqueValidatorResolutionCausesCompilationError() {

		File sourceFile1 = compilerHelper.getSourceFile( NoUniqueValidatorResolution.class );
		File sourceFile2 = compilerHelper.getSourceFile( Size.class );
		File sourceFile3 = compilerHelper.getSourceFile( SizeValidatorForCollection.class );
		File sourceFile4 = compilerHelper.getSourceFile( SizeValidatorForSerializable.class );
		File sourceFile5 = compilerHelper.getSourceFile( SerializableCollection.class );
		File sourceFile6 = compilerHelper.getSourceFile( SizeValidatorForSet.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						sourceFile1,
						sourceFile2,
						sourceFile3,
						sourceFile4,
						sourceFile5,
						sourceFile6
				);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics, new DiagnosticExpection(
						Kind.ERROR, 33
				)
		);
	}

	@Test
	public void testThatMultiValuedConstrainedIsOnlyGivenAtSupportedType() {

		File sourceFile1 = compilerHelper.getSourceFile( MultipleConstraintsOfSameType.class );

		boolean compilationResult = compilerHelper.compile(
				new ConstraintValidationProcessor(), diagnostics, sourceFile1
		);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpection( Kind.ERROR, 33 ),
				new DiagnosticExpection( Kind.ERROR, 33 )
		);
	}

	@Test
	public void testThatAtValidAnnotationGivenAtNotSupportedTypesCausesCompilationErrors() {

		File sourceFile1 = compilerHelper.getSourceFile( ValidationUsingAtValidAnnotation.class );

		boolean compilationResult = compilerHelper.compile(
				new ConstraintValidationProcessor(), diagnostics, sourceFile1
		);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpection( Kind.ERROR, 34 ),
				new DiagnosticExpection( Kind.ERROR, 40 ),
				new DiagnosticExpection( Kind.ERROR, 56 ),
				new DiagnosticExpection( Kind.ERROR, 64 ),
				new DiagnosticExpection( Kind.ERROR, 72 ),
				new DiagnosticExpection( Kind.ERROR, 80 ),
				new DiagnosticExpection( Kind.ERROR, 88 )
		);
	}
}
