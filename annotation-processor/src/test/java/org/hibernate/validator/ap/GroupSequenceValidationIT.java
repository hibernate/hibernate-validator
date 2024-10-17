/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
public class GroupSequenceValidationIT extends ConstraintValidationProcessorITBase {

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
				new DiagnosticExpectation( Kind.ERROR, 27 ),
				new DiagnosticExpectation( Kind.ERROR, 31 ),
				new DiagnosticExpectation( Kind.ERROR, 46 ),
				new DiagnosticExpectation( Kind.ERROR, 50 ),
				new DiagnosticExpectation( Kind.WARNING, 65 ),
				new DiagnosticExpectation( Kind.ERROR, 83 ),
				new DiagnosticExpectation( Kind.ERROR, 92 ),
				new DiagnosticExpectation( Kind.ERROR, 96 ),
				new DiagnosticExpectation( Kind.ERROR, 111 ),
				new DiagnosticExpectation( Kind.ERROR, 126 ),
				new DiagnosticExpectation( Kind.ERROR, 130 ),
				new DiagnosticExpectation( Kind.ERROR, 134 ),
				new DiagnosticExpectation( Kind.ERROR, 149 ),
				new DiagnosticExpectation( Kind.ERROR, 172 )
		);
	}

}
