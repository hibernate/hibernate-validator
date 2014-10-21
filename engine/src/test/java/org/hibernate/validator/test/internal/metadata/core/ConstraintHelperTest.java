/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.metadata.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import javax.validation.constraints.Pattern;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.test.internal.metadata.Engine;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintHelperTest {

	private static ConstraintHelper constraintHelper;

	@BeforeClass
	public static void init() {
		constraintHelper = new ConstraintHelper();
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
		assertTrue( multiValueConstraintAnnotations.size() == 2, "There should be two constraint annotations" );

		assertTrue( multiValueConstraintAnnotations.get( 0 ) instanceof Pattern, "Wrong constraint annotation" );
		assertEquals( ( (Pattern) multiValueConstraintAnnotations.get( 0 ) ).regexp(), "^[A-Z0-9-]+$" );

		assertTrue( multiValueConstraintAnnotations.get( 1 ) instanceof Pattern, "Wrong constraint annotation" );
		assertEquals( ( (Pattern) multiValueConstraintAnnotations.get( 1 ) ).regexp(), "^....-....-....$" );
	}
}
