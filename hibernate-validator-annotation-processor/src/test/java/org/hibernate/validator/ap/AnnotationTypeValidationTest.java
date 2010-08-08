// $Id: AnnotationTypeValidationTest.java 19525 2010-05-15 16:05:09Z gunnar.morling $
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

		File sourceFile = compilerHelper.getSourceFile( ConstraintsWithIllegalRetentionPolicies.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 33 ), new DiagnosticExpectation( Kind.ERROR, 42 )
		);
	}

	@Test
	public void testThatConstraintAnnotationTypeWithWrongTargetCausesCompilationError() {

		File sourceFile = compilerHelper.getSourceFile( ConstraintsWithIllegalTargets.class );

		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics, sourceFile );

		assertFalse( compilationResult );
		assertThatDiagnosticsMatch(
				diagnostics,
				new DiagnosticExpectation( Kind.ERROR, 42 ), new DiagnosticExpectation( Kind.ERROR, 52 )
		);
	}


}
