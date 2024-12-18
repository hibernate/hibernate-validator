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

import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithIllegalRetentionPolicies;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithIllegalTargets;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithWrongGroupsAttribute;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithWrongMessageAttribute;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithWrongPayloadAttribute;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithoutValidator;
import org.hibernate.validator.ap.testmodel.constrainttypes.DummyValidator;
import org.hibernate.validator.ap.testmodel.constrainttypes.ValidCustomerNumber;
import org.hibernate.validator.ap.testmodel.crossparameters.DoubleValidatorConstraint;
import org.hibernate.validator.ap.testmodel.crossparameters.DoubleValidatorDummyValidator;
import org.hibernate.validator.ap.testmodel.crossparameters.GenericCrossParameterValidator;
import org.hibernate.validator.ap.testmodel.crossparameters.GenericCrossParameterValidatorObjectArray;
import org.hibernate.validator.ap.testmodel.crossparameters.GenericNormalValidator;
import org.hibernate.validator.ap.testmodel.crossparameters.InvalidValidator;
import org.hibernate.validator.ap.testmodel.crossparameters.InvalidValidatorConstraint;
import org.hibernate.validator.ap.testmodel.crossparameters.MixDirectAnnotationAndListContainerAnnotation;
import org.hibernate.validator.ap.testmodel.crossparameters.ValidCrossParameterAndNormalConstraint;
import org.hibernate.validator.ap.testmodel.crossparameters.ValidCrossParameterConstraint;
import org.hibernate.validator.ap.testmodel.crossparameters.ValidCrossParameterConstraintWithObjectArrayValidator;
import org.hibernate.validator.ap.testmodel.crossparameters.WrongValidationAppliesToConstraintWithInvalidDefault;
import org.hibernate.validator.ap.testmodel.crossparameters.WrongValidationAppliesToConstraintWithInvalidReturnType;
import org.hibernate.validator.ap.testmodel.crossparameters.WrongValidationAppliesToConstraintWithMissingAttribute;
import org.hibernate.validator.ap.util.DiagnosticExpectation;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * Test cases for {@link ConstraintValidationProcessor} testing the checking of constraint
 * annotation type declarations.
 *
 * @author Gunnar Morling
 */
public class AnnotationTypeValidationIT extends ConstraintValidationProcessorITBase {

	@Test
	public void testThatSpecifyingConstraintAnnotationAtNonConstraintAnnotationTypeCausesCompilationError() {

		File sourceFile = compilerHelper.getSourceFile( ValidCustomerNumber.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics, new DiagnosticExpectation( Kind.ERROR, 15 ), new DiagnosticExpectation( Kind.ERROR, 16 )
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
				new DiagnosticExpectation( Kind.ERROR, 22 ), new DiagnosticExpectation( Kind.ERROR, 37 )
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
				new DiagnosticExpectation( Kind.ERROR, 31 ), new DiagnosticExpectation( Kind.ERROR, 47 )
		);
	}

	@Test
	public void testThatConstraintAnnotationTypeWithoutValidatorCausesCompilationError() {

		File sourceFile1 = compilerHelper.getSourceFile( ConstraintsWithoutValidator.class );
		File sourceFile2 = compilerHelper.getSourceFile( DummyValidator.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile1, sourceFile2 );

		assertTrue( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.WARNING, 23 )
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
				new DiagnosticExpectation( Kind.ERROR, 22 ), new DiagnosticExpectation( Kind.ERROR, 37 )
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
				new DiagnosticExpectation( Kind.ERROR, 23 ),
				new DiagnosticExpectation( Kind.ERROR, 40 ),
				new DiagnosticExpectation( Kind.ERROR, 55 ),
				new DiagnosticExpectation( Kind.ERROR, 70 ),
				new DiagnosticExpectation( Kind.ERROR, 85 ),
				new DiagnosticExpectation( Kind.ERROR, 100 ),
				new DiagnosticExpectation( Kind.ERROR, 115 )
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
				new DiagnosticExpectation( Kind.ERROR, 23 ),
				new DiagnosticExpectation( Kind.ERROR, 41 ),
				new DiagnosticExpectation( Kind.ERROR, 56 ),
				new DiagnosticExpectation( Kind.ERROR, 71 ),
				new DiagnosticExpectation( Kind.ERROR, 86 ),
				new DiagnosticExpectation( Kind.ERROR, 101 ),
				new DiagnosticExpectation( Kind.ERROR, 116 ),
				new DiagnosticExpectation( Kind.ERROR, 131 )
		);
	}

	@Test
	public void testThatConstraintAnnotationWithMultipleCrossParameterValidatorsCausesCompilationError() {

		File[] sourceFiles = new File[] {
				compilerHelper.getSourceFile( DoubleValidatorConstraint.class ),
				compilerHelper.getSourceFile( GenericCrossParameterValidator.class ),
				compilerHelper.getSourceFile( DoubleValidatorDummyValidator.class )
		};

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFiles );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 21 )
		);
	}

	@Test
	public void testThatConstraintAnnotationWithInvalidCrossParameterValidatorsCausesCompilationError() {

		File[] sourceFiles = new File[] {
				compilerHelper.getSourceFile( InvalidValidatorConstraint.class ),
				compilerHelper.getSourceFile( InvalidValidator.class )
		};

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFiles );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 21 )
		);
	}

	@Test
	public void testThatCrossParameterConstraintWithInvalidDefaultInValidationAppliesToCausesCompilationError() {

		File[] sourceFiles = new File[] {
				compilerHelper.getSourceFile( WrongValidationAppliesToConstraintWithInvalidDefault.class ),
				compilerHelper.getSourceFile( GenericCrossParameterValidator.class )
		};

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFiles );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 22 )
		);
	}

	@Test
	public void testThatCrossParameterConstraintWithInvalidReturnTypeInValidationAppliesToCausesCompilationError() {

		File[] sourceFiles = new File[] {
				compilerHelper.getSourceFile( WrongValidationAppliesToConstraintWithInvalidReturnType.class ),
				compilerHelper.getSourceFile( GenericCrossParameterValidator.class )
		};

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFiles );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 21 )
		);
	}

	@Test
	public void testThatCrossParameterConstraintWithoutRequiredValidationAppliesToCausesCompilationError() {

		File[] sourceFiles = new File[] {
				compilerHelper.getSourceFile( WrongValidationAppliesToConstraintWithMissingAttribute.class ),
				compilerHelper.getSourceFile( GenericCrossParameterValidator.class ),
				compilerHelper.getSourceFile( GenericNormalValidator.class )
		};

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFiles );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 24 )
		);
	}

	@Test
	public void testThatValidCrossParameterConstraintsAreCompiledCorrectly() {

		File[] sourceFiles = new File[] {
				compilerHelper.getSourceFile( ValidCrossParameterConstraint.class ),
				compilerHelper.getSourceFile( ValidCrossParameterConstraintWithObjectArrayValidator.class ),
				compilerHelper.getSourceFile( ValidCrossParameterAndNormalConstraint.class ),
				compilerHelper.getSourceFile( GenericCrossParameterValidator.class ),
				compilerHelper.getSourceFile( GenericCrossParameterValidatorObjectArray.class ),
				compilerHelper.getSourceFile( GenericNormalValidator.class )
		};

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFiles );

		assertTrue( compilationResult );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1275")
	public void testMixDirectAnnotationAndListContainer() {
		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics,
						compilerHelper.getSourceFile( MixDirectAnnotationAndListContainerAnnotation.class )
				);

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 30 )
		);
	}

}
