/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.record;

import static org.hibernate.validator.ap.testutil.CompilerTestHelper.assertThatDiagnosticsMatch;
import static org.testng.Assert.assertFalse;

import java.io.File;
import java.util.EnumSet;

import javax.tools.Diagnostic;

import org.hibernate.validator.ap.ConstraintValidationProcessor;
import org.hibernate.validator.ap.ConstraintValidationProcessorITBase;
import org.hibernate.validator.ap.testutil.CompilerTestHelper;
import org.hibernate.validator.ap.util.DiagnosticExpectation;

import org.testng.annotations.Test;

/**
 * @author Jan Schatteman
 */
public class RecordConstraintValidationProcessorIT extends ConstraintValidationProcessorITBase {

	@Test
	public void testRecordWithInvalidConstraints() {

		File sourceFile = compilerHelper.getSourceFile( RecordWithInvalidConstraints.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						EnumSet.of( CompilerTestHelper.Library.VALIDATION_API ),
						sourceFile
				);

		assertFalse( compilationResult );

		// given the nature of the records, a second error is thrown at line -1:
		// "The annotation @FutureOrPresent is disallowed for the return type of this method."
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Diagnostic.Kind.ERROR, -1 ),
				new DiagnosticExpectation( Diagnostic.Kind.ERROR, 16 )
		);
	}

	@Test
	public void testRecordWithInvalidConstructorConstraints() {

		File sourceFile = compilerHelper.getSourceFile( RecordWithInvalidConstructorConstraints.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						EnumSet.of( CompilerTestHelper.Library.VALIDATION_API ),
						sourceFile
				);

		assertFalse( compilationResult );

		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Diagnostic.Kind.ERROR, 17 )
		);
	}

	@Test
	public void testRecordWithInvalidMethodConstraints() {

		File sourceFile = compilerHelper.getSourceFile( RecordWithInvalidMethodConstraints.class );

		boolean compilationResult =
				compilerHelper.compile(
						new ConstraintValidationProcessor(),
						diagnostics,
						EnumSet.of( CompilerTestHelper.Library.VALIDATION_API ),
						sourceFile
				);

		assertFalse( compilationResult );

		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Diagnostic.Kind.ERROR, 19 )
		);
	}
}
