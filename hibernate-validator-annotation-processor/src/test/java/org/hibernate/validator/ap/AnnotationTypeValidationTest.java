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

import org.testng.annotations.Test;

import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithIllegalRetentionPolicies;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithIllegalTargets;
import org.hibernate.validator.ap.testmodel.constrainttypes.ConstraintsWithWrongMessageAttribute;
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
 * @author Gunnar Morling.
 */
public class AnnotationTypeValidationTest extends ConstraintValidationProcessorTestBase {

	@Test
	public void testThatSpecifyingConstraintAnnotationAtNonConstraintAnnotationTypeCausesCompilationError() {

		File sourceFile = compilerHelper.getSourceFile( ValidCustomerNumber.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics, new DiagnosticExpectation( Kind.ERROR, 28 ), new DiagnosticExpectation( Kind.ERROR, 29 )
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
				new DiagnosticExpectation( Kind.ERROR, 34 ), new DiagnosticExpectation( Kind.ERROR, 49 )
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
				new DiagnosticExpectation( Kind.ERROR, 43 ), new DiagnosticExpectation( Kind.ERROR, 59 )
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
				new DiagnosticExpectation( Kind.ERROR, 35 )
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
				new DiagnosticExpectation( Kind.ERROR, 35 ), new DiagnosticExpectation( Kind.ERROR, 50 )
		);
	}
}
