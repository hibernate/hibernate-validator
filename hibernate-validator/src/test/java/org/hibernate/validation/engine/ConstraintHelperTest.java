// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

import org.hibernate.validation.constraints.invalidvalidators.NoGroups;
import org.hibernate.validation.constraints.invalidvalidators.NoMessage;
import org.hibernate.validation.constraints.invalidvalidators.ValidProperty;
import org.hibernate.validation.eg.Engine;
import org.hibernate.validation.eg.Order;
import org.hibernate.validation.util.ReflectionHelper;

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
	public void testConstraintWithNoMessage() {
		Annotation annotation = new NoGroups() {
			public String message() {
				return "";
			}

			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}
		};
		assertFalse(
				"The constraint annotation should not be valid", constraintHelper.isConstraintAnnotation( annotation )
		);
	}

	@Test
	public void testConstraintWithNoGroups() {
		Annotation annotation = new NoMessage() {
			public Class<?>[] groups() {
				return null;
			}

			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}
		};
		assertFalse(
				"The constraint annotation should not be valid", constraintHelper.isConstraintAnnotation( annotation )
		);
	}

	@Test
	public void testConstraintWithValidInPropertyName() {
		Annotation annotation = new ValidProperty() {
			public String message() {
				return null;
			}

			public Class<?>[] groups() {
				return null;
			}

			public int validLength() {
				return 0;
			}

			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}
		};
		assertFalse(
				"The constraint annotation should not be valid", constraintHelper.isConstraintAnnotation( annotation )
		);
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
		assertTrue( "There should be two constraint annotations", multiValueConstraintAnnotations.size() == 2 );
		assertTrue( "Wrong constraint annotation", multiValueConstraintAnnotations.get( 0 ) instanceof Pattern );
		assertTrue( "Wrong constraint annotation", multiValueConstraintAnnotations.get( 1 ) instanceof Pattern );


		Order order = new Order();
		fields = order.getClass().getDeclaredFields();
		assertNotNull( fields );
		assertTrue( fields.length == 1 );
		ReflectionHelper.setAccessibility( fields[0] );

		annotation = fields[0].getAnnotation( NotNull.class );
		assertNotNull( annotation );
		multiValueConstraintAnnotations = constraintHelper.getMultiValueConstraints( annotation );
		assertTrue( "There should be no constraint annotations", multiValueConstraintAnnotations.size() == 0 );
	}
}
