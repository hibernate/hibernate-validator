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

import org.hibernate.validator.ap.testmodel.annotationparameters.InvalidGroupSequenceParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.ValidGroupSequenceParameters;
import org.hibernate.validator.ap.util.DiagnosticExpectation;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

/**
 * Test cases for {@link ConstraintValidationProcessor} testing the checking of group sequences validity.
 *
 * @author Marko Bekhta
 */
public class GroupSequenceValidationTest extends ConstraintValidationProcessorTestBase {

	@Test
	@TestForIssue(jiraKey = "HV-451")
	public void testValidGroupSequenceParameter() {
		File sourceFile = compilerHelper.getSourceFile( ValidGroupSequenceParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertTrue( compilationResult );
		assertThatDiagnosticsMatch( diagnostics );
	}

	@Test
	@TestForIssue(jiraKey = "HV-451")
	public void testInvalidGroupSequenceParameter() {
		File sourceFile = compilerHelper.getSourceFile( InvalidGroupSequenceParameters.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 29 ),
				new DiagnosticExpectation( Kind.ERROR, 33 ),
				new DiagnosticExpectation( Kind.ERROR, 48 ),
				new DiagnosticExpectation( Kind.ERROR, 52 ),
				new DiagnosticExpectation( Kind.WARNING, 67 ),
				new DiagnosticExpectation( Kind.ERROR, 85 ),
				new DiagnosticExpectation( Kind.ERROR, 94 ),
				new DiagnosticExpectation( Kind.ERROR, 98 ),
				new DiagnosticExpectation( Kind.ERROR, 113 ),
				new DiagnosticExpectation( Kind.ERROR, 128 ),
				new DiagnosticExpectation( Kind.ERROR, 132 ),
				new DiagnosticExpectation( Kind.ERROR, 136 ),
				new DiagnosticExpectation( Kind.ERROR, 151 ),
				new DiagnosticExpectation( Kind.ERROR, 174 )
		);
	}

}
