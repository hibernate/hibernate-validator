/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap;

import static org.testng.Assert.assertTrue;

import org.hibernate.validator.ap.testmodel.ciruclar.Property;
import org.hibernate.validator.ap.testmodel.ciruclar.PropertyImpl;

import org.testng.annotations.Test;

/**
 * Tests that in case of circular nested types there's no infinite loop during analysis.
 *
 * @author Marko Bekhta
 */
public class CircularNestedTypesTest extends ConstraintValidationProcessorTestBase {

	@Test
	public void testNoInfiniteLoop() {
		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics,
						compilerHelper.getSourceFile( PropertyImpl.class ),
						compilerHelper.getSourceFile( Property.class )
				);

		assertTrue( compilationResult );
	}

}
