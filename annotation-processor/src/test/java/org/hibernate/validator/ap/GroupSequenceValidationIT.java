package org.hibernate.validator.ap;

import static org.hibernate.validator.ap.testutil.CompilerTestHelper.assertThatDiagnosticsMatch;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import javax.tools.Diagnostic.Kind;

import org.hibernate.validator.ap.testmodel.annotationparameters.InvalidGroupSequenceParameters;
import org.hibernate.validator.ap.testmodel.annotationparameters.ValidGroupSequenceParameters;
import org.hibernate.validator.ap.util.DiagnosticExpectation;
import org.hibernate.validator.testutil.TestForIssue;

import org.junit.jupiter.api.Test;

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
				new DiagnosticExpectation( Kind.ERROR, 23 ),
				new DiagnosticExpectation( Kind.ERROR, 27 ),
				new DiagnosticExpectation( Kind.ERROR, 42 ),
				new DiagnosticExpectation( Kind.ERROR, 46 ),
				new DiagnosticExpectation( Kind.WARNING, 61 ),
				new DiagnosticExpectation( Kind.ERROR, 79 ),
				new DiagnosticExpectation( Kind.ERROR, 88 ),
				new DiagnosticExpectation( Kind.ERROR, 92 ),
				new DiagnosticExpectation( Kind.ERROR, 107 ),
				new DiagnosticExpectation( Kind.ERROR, 122 ),
				new DiagnosticExpectation( Kind.ERROR, 126 ),
				new DiagnosticExpectation( Kind.ERROR, 130 ),
				new DiagnosticExpectation( Kind.ERROR, 145 ),
				new DiagnosticExpectation( Kind.ERROR, 168 )
		);
	}

}
