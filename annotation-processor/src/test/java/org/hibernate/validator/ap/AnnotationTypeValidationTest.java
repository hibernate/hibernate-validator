/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap;

import java.io.File;
import javax.tools.Diagnostic.Kind;

import org.testng.annotations.Test;

import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithIllegalRetentionPolicies;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithIllegalTargets;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithWrongGroupsAttribute;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithWrongMessageAttribute;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithWrongPayloadAttribute;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithoutValidator;
import org.hibernate.validator.ap.testmodel.constrainttypes.DummyValidator;
import org.hibernate.validator.ap.testmodel.constrainttypes.ValidCustomerNumber;
import org.hibernate.validator.ap.util.DiagnosticExpectation;

import static org.hibernate.validator.ap.testutil.CompilerTestHelper.assertThatDiagnosticsMatch;
import static org.testng.Assert.assertFalse;

/**
 * Test cases for {@link ConstraintValidationProcessor} testing the checking of constraint
 * annotation type declarations.
 *
 * @author Gunnar Morling
 */
public class AnnotationTypeValidationTest extends ConstraintValidationProcessorTestBase {

	@Test
	public void testThatSpecifyingConstraintAnnotationAtNonConstraintAnnotationTypeCausesCompilationError() {

		File sourceFile = compilerHelper.getSourceFile( ValidCustomerNumber.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics, new DiagnosticExpectation( Kind.ERROR, 17 ), new DiagnosticExpectation( Kind.ERROR, 18 )
		);
	}

	@Test
	public void testThatConstraintAnnotationTypeWithWrongRetentionPolicyCausesCompilationError() {

		File sourceFile1 = compilerHelper.getSourceFile( ConstraintsWithIllegalRetentionPolicies.class );
		File sourceFile2 = compilerHelper.getSourceFile( DummyValidator.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2 );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 23 ), new DiagnosticExpectation( Kind.ERROR, 38 )
		);
	}

	@Test
	public void testThatConstraintAnnotationTypeWithWrongTargetCausesCompilationError() {

		File sourceFile1 = compilerHelper.getSourceFile( ConstraintsWithIllegalTargets.class );
		File sourceFile2 = compilerHelper.getSourceFile( DummyValidator.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2 );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 32 ), new DiagnosticExpectation( Kind.ERROR, 48 )
		);
	}

	@Test
	public void testThatConstraintAnnotationTypeWithoutValidatorCausesCompilationError() {

		File sourceFile1 = compilerHelper.getSourceFile( ConstraintsWithoutValidator.class );
		File sourceFile2 = compilerHelper.getSourceFile( DummyValidator.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2 );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 24 )
		);
	}

	@Test
	public void testThatConstraintAnnotationTypeWithMissingOrWrongMessageAttributeCausesCompilationError() {

		File sourceFile1 = compilerHelper.getSourceFile( ConstraintsWithWrongMessageAttribute.class );
		File sourceFile2 = compilerHelper.getSourceFile( DummyValidator.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2 );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 23 ), new DiagnosticExpectation( Kind.ERROR, 38 )
		);
	}

	@Test
	public void testThatConstraintAnnotationTypeWithMissingOrWrongGroupsAttributeCausesCompilationError() {

		File sourceFile1 = compilerHelper.getSourceFile( ConstraintsWithWrongGroupsAttribute.class );
		File sourceFile2 = compilerHelper.getSourceFile( DummyValidator.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2 );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 24 ),
				new DiagnosticExpectation( Kind.ERROR, 41 ),
				new DiagnosticExpectation( Kind.ERROR, 56 ),
				new DiagnosticExpectation( Kind.ERROR, 71 ),
				new DiagnosticExpectation( Kind.ERROR, 86 ),
				new DiagnosticExpectation( Kind.ERROR, 101 ),
				new DiagnosticExpectation( Kind.ERROR, 116 )
		);
	}

	@Test
	public void testThatConstraintAnnotationTypeWithMissingOrPayloadGroupsAttributeCausesCompilationError() {

		File sourceFile1 = compilerHelper.getSourceFile( ConstraintsWithWrongPayloadAttribute.class );
		File sourceFile2 = compilerHelper.getSourceFile( DummyValidator.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2 );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 24 ),
				new DiagnosticExpectation( Kind.ERROR, 42 ),
				new DiagnosticExpectation( Kind.ERROR, 57 ),
				new DiagnosticExpectation( Kind.ERROR, 72 ),
				new DiagnosticExpectation( Kind.ERROR, 87 ),
				new DiagnosticExpectation( Kind.ERROR, 102 ),
				new DiagnosticExpectation( Kind.ERROR, 117 ),
				new DiagnosticExpectation( Kind.ERROR, 132 )
		);
	}

}
