/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
