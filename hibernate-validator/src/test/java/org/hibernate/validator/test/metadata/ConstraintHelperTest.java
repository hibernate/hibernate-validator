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
package org.hibernate.validator.test.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.metadata.ConstraintHelper;
import org.hibernate.validator.util.ReflectionHelper;

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
	public void testGetMultiValueConstraints() throws Exception {
		Engine engine = new Engine();
		Field[] fields = engine.getClass().getDeclaredFields();
		assertNotNull( fields );
		assertTrue( fields.length == 1 );
		ReflectionHelper.setAccessibility( fields[0] );

		Annotation annotation = fields[0].getAnnotation( Pattern.List.class );
		assertNotNull( annotation );
		List<Annotation> multiValueConstraintAnnotations = constraintHelper.getMultiValueConstraints( annotation );
		assertTrue( multiValueConstraintAnnotations.size() == 2, "There should be two constraint annotations" );
		assertTrue( multiValueConstraintAnnotations.get( 0 ) instanceof Pattern, "Wrong constraint annotation" );
		assertTrue( multiValueConstraintAnnotations.get( 1 ) instanceof Pattern, "Wrong constraint annotation" );


		Order order = new Order();
		fields = order.getClass().getDeclaredFields();
		assertNotNull( fields );
		assertTrue( fields.length == 1 );
		ReflectionHelper.setAccessibility( fields[0] );

		annotation = fields[0].getAnnotation( NotNull.class );
		assertNotNull( annotation );
		multiValueConstraintAnnotations = constraintHelper.getMultiValueConstraints( annotation );
		assertTrue( multiValueConstraintAnnotations.size() == 0, "There should be no constraint annotations" );
	}
}
