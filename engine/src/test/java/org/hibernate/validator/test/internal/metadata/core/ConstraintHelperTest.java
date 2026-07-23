/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.metadata.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;

import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.test.internal.metadata.Engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * @author Hardy Ferentschik
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConstraintHelperTest {

	private static ConstraintHelper constraintHelper;

	@BeforeAll
	public static void init() {
		constraintHelper = ConstraintHelper.forAllBuiltinConstraints();
	}

	@Test
	public void testIsMultiValueConstraintRecognizesMultiValueConstraint() {
		assertTrue( constraintHelper.isMultiValueConstraint( Pattern.List.class ) );
	}

	@Test
	public void testIsMultiValueConstraintRecognizesNonMultiValueConstraint() {
		assertFalse( constraintHelper.isMultiValueConstraint( Pattern.class ) );
	}

	@Test
	public void testGetConstraintsFromMultiValueConstraint() throws Exception {
		Engine engine = new Engine();
		Field field = engine.getClass().getDeclaredField( "serialNumber" );

		Annotation annotation = field.getAnnotation( Pattern.List.class );
		assertNotNull( annotation );
		List<Annotation> multiValueConstraintAnnotations = constraintHelper.getConstraintsFromMultiValueConstraint(
				annotation
		);
		assertEquals( 2, multiValueConstraintAnnotations.size(), "There should be two constraint annotations" );

		assertInstanceOf( Pattern.class, multiValueConstraintAnnotations.get( 0 ), "Wrong constraint annotation" );
		assertEquals( "^[A-Z0-9-]+$", ( (Pattern) multiValueConstraintAnnotations.get( 0 ) ).regexp() );

		assertInstanceOf( Pattern.class, multiValueConstraintAnnotations.get( 1 ), "Wrong constraint annotation" );
		assertEquals( "^....-....-....$", ( (Pattern) multiValueConstraintAnnotations.get( 1 ) ).regexp() );
	}
}
