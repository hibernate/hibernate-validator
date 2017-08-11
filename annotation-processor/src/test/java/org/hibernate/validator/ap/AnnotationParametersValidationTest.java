/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap;

import static org.hibernate.validator.ap.testutil.CompilerTestHelper.assertThatDiagnosticsMatch;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;

import javax.tools.Diagnostic.Kind;

import org.hibernate.validator.ap.testmodel.annotationparameters.InvalidDecimalMinMaxParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.InvalidDigitsParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.InvalidLengthParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.InvalidPatternParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.InvalidScriptAssertParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.InvalidSizeParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.InvalidUnwrappingCombination;
import org.hibernate.validator.ap.testmodel.annotationparameters.ValidDecimalMinMaxParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.ValidDigitsParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.ValidGroupsParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.ValidLengthParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.ValidMessageParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.ValidPatternParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.ValidScriptAssertParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.ValidSizeParameters;
import org.hibernate.validator.ap.util.DiagnosticExpectation;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * Test cases for {@link ConstraintValidationProcessor} testing the checking of annotation parameters validity.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersValidationTest extends ConstraintValidationProcessorTestBase {

	@Test
	public void testValidSizeParameters() {
		File sourceFile = compilerHelper.getSourceFile( ValidSizeParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertTrue( compilationResult );
		assertThatDiagnosticsMatch( diagnostics );
	}

	@Test
	public void testInvalidSizeParameters() {
		File sourceFile = compilerHelper.getSourceFile( InvalidSizeParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 17 ),
				new DiagnosticExpectation( Kind.ERROR, 20 ),
				new DiagnosticExpectation( Kind.ERROR, 23 ),
				new DiagnosticExpectation( Kind.ERROR, 26 ),
				new DiagnosticExpectation( Kind.ERROR, 30 ),
				new DiagnosticExpectation( Kind.ERROR, 31 ),
				new DiagnosticExpectation( Kind.ERROR, 32 ),
				new DiagnosticExpectation( Kind.ERROR, 38 ),
				new DiagnosticExpectation( Kind.ERROR, 39 ),
				new DiagnosticExpectation( Kind.ERROR, 40 ),
				new DiagnosticExpectation( Kind.ERROR, 45 )
		);
	}

	@Test
	public void testValidLengthParameters() {
		File sourceFile = compilerHelper.getSourceFile( ValidLengthParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertTrue( compilationResult );
		assertThatDiagnosticsMatch( diagnostics );
	}

	@Test
	public void testInvalidLengthParameters() {
		File sourceFile = compilerHelper.getSourceFile( InvalidLengthParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 16 ),
				new DiagnosticExpectation( Kind.ERROR, 19 ),
				new DiagnosticExpectation( Kind.ERROR, 22 ),
				new DiagnosticExpectation( Kind.ERROR, 25 ),
				new DiagnosticExpectation( Kind.ERROR, 29 ),
				new DiagnosticExpectation( Kind.ERROR, 30 ),
				new DiagnosticExpectation( Kind.ERROR, 31 ),
				new DiagnosticExpectation( Kind.ERROR, 37 ),
				new DiagnosticExpectation( Kind.ERROR, 38 ),
				new DiagnosticExpectation( Kind.ERROR, 39 ),
				new DiagnosticExpectation( Kind.ERROR, 44 )
		);
	}

	@Test
	public void testValidScriptAssertParameters() {
		File sourceFile = compilerHelper.getSourceFile( ValidScriptAssertParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertTrue( compilationResult );
		assertThatDiagnosticsMatch( diagnostics );
	}

	@Test
	public void testInvalidScriptAssertParameters() {
		File sourceFile = compilerHelper.getSourceFile( InvalidScriptAssertParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 16 ),
				new DiagnosticExpectation( Kind.ERROR, 20 ),
				new DiagnosticExpectation( Kind.ERROR, 24 ),
				new DiagnosticExpectation( Kind.ERROR, 28 ),
				new DiagnosticExpectation( Kind.ERROR, 32 ),
				new DiagnosticExpectation( Kind.ERROR, 36 ),
				new DiagnosticExpectation( Kind.ERROR, 40 ),
				new DiagnosticExpectation( Kind.ERROR, 44 ),
				new DiagnosticExpectation( Kind.ERROR, 48 ),
				new DiagnosticExpectation( Kind.ERROR, 52 ),
				new DiagnosticExpectation( Kind.ERROR, 56 )
		);
	}

	@Test
	public void testValidPatternParameters() {
		File sourceFile = compilerHelper.getSourceFile( ValidPatternParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertTrue( compilationResult );
		assertThatDiagnosticsMatch( diagnostics );
	}

	@Test
	public void testInvalidPatternParameters() {
		File sourceFile = compilerHelper.getSourceFile( InvalidPatternParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 16 ),
				new DiagnosticExpectation( Kind.ERROR, 19 ),
				new DiagnosticExpectation( Kind.ERROR, 22 ),
				new DiagnosticExpectation( Kind.ERROR, 25 ),
				new DiagnosticExpectation( Kind.ERROR, 29 ),
				new DiagnosticExpectation( Kind.ERROR, 30 ),
				new DiagnosticExpectation( Kind.ERROR, 31 ),
				new DiagnosticExpectation( Kind.ERROR, 37 ),
				new DiagnosticExpectation( Kind.ERROR, 38 ),
				new DiagnosticExpectation( Kind.ERROR, 39 ),
				new DiagnosticExpectation( Kind.ERROR, 44 )
		);
	}

	@Test
	public void testValidDigitsParameters() {
		File sourceFile = compilerHelper.getSourceFile( ValidDigitsParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertTrue( compilationResult );
		assertThatDiagnosticsMatch( diagnostics );
	}

	@Test
	public void testInvalidDigitsParameters() {
		File sourceFile = compilerHelper.getSourceFile( InvalidDigitsParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 17 ),
				new DiagnosticExpectation( Kind.ERROR, 20 ),
				new DiagnosticExpectation( Kind.ERROR, 23 ),
				new DiagnosticExpectation( Kind.ERROR, 26 ),
				new DiagnosticExpectation( Kind.ERROR, 30 ),
				new DiagnosticExpectation( Kind.ERROR, 31 ),
				new DiagnosticExpectation( Kind.ERROR, 32 ),
				new DiagnosticExpectation( Kind.ERROR, 38 ),
				new DiagnosticExpectation( Kind.ERROR, 39 ),
				new DiagnosticExpectation( Kind.ERROR, 40 ),
				new DiagnosticExpectation( Kind.ERROR, 45 )
		);
	}

	@Test
	public void testValidDecimalMinMaxParameters() {
		File sourceFile = compilerHelper.getSourceFile( ValidDecimalMinMaxParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertTrue( compilationResult );
		assertThatDiagnosticsMatch( diagnostics );
	}

	@Test
	public void testInvalidDecimalMinMaxParameters() {
		File sourceFile = compilerHelper.getSourceFile( InvalidDecimalMinMaxParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 18 ),
				new DiagnosticExpectation( Kind.ERROR, 19 ),
				new DiagnosticExpectation( Kind.ERROR, 22 ),
				new DiagnosticExpectation( Kind.ERROR, 23 ),
				new DiagnosticExpectation( Kind.ERROR, 26 ),
				new DiagnosticExpectation( Kind.ERROR, 27 ),
				new DiagnosticExpectation( Kind.ERROR, 30 ),
				new DiagnosticExpectation( Kind.ERROR, 31 ),
				new DiagnosticExpectation( Kind.ERROR, 35 ),
				new DiagnosticExpectation( Kind.ERROR, 36 ),
				new DiagnosticExpectation( Kind.ERROR, 37 ),
				new DiagnosticExpectation( Kind.ERROR, 43 ),
				new DiagnosticExpectation( Kind.ERROR, 44 ),
				new DiagnosticExpectation( Kind.ERROR, 45 ),
				new DiagnosticExpectation( Kind.ERROR, 50 )
		);
	}

	@Test
	public void testValidGroupsParameter() {
		File sourceFile = compilerHelper.getSourceFile( ValidGroupsParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 25 ),
				new DiagnosticExpectation( Kind.ERROR, 28 ),
				new DiagnosticExpectation( Kind.ERROR, 31 ),
				new DiagnosticExpectation( Kind.ERROR, 36 ),
				new DiagnosticExpectation( Kind.ERROR, 41 ),
				new DiagnosticExpectation( Kind.ERROR, 53 )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-822")
	public void testValidMessageParameter() {
		File sourceFile = compilerHelper.getSourceFile( ValidMessageParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertTrue( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.WARNING, 57 ),
				new DiagnosticExpectation( Kind.WARNING, 88 ),
				new DiagnosticExpectation( Kind.WARNING, 98 ),
				new DiagnosticExpectation( Kind.WARNING, 101 ),
				new DiagnosticExpectation( Kind.WARNING, 108 ),
				new DiagnosticExpectation( Kind.WARNING, 109 ),
				new DiagnosticExpectation( Kind.WARNING, 110 ),
				new DiagnosticExpectation( Kind.WARNING, 122 )
		);
	}

	@Test
	public void usingIncompatibleUnwrappingCombination() {
		File sourceFile = compilerHelper.getSourceFile( InvalidUnwrappingCombination.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 29 ),
				new DiagnosticExpectation( Kind.WARNING, 35 )
		);
	}

}
