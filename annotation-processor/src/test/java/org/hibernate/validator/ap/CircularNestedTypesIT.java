/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hibernate.validator.ap.testmodel.circular.CircularProperty;
import org.hibernate.validator.ap.testmodel.circular.CircularPropertyImpl;

import org.junit.jupiter.api.Test;

/**
 * Tests that in case of circular nested types there's no infinite loop during analysis.
 *
 * @author Marko Bekhta
 */
public class CircularNestedTypesIT extends ConstraintValidationProcessorITBase {

	@Test
	public void testNoInfiniteLoop() {
		boolean compilationResult =
				compilerHelper.compile( new ConstraintValidationProcessor(), diagnostics,
						compilerHelper.getSourceFile( CircularPropertyImpl.class ),
						compilerHelper.getSourceFile( CircularProperty.class )
				);

		assertTrue( compilationResult );
	}
}
