/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.util.privilegedactions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.annotation.Annotation;

import javax.validation.Payload;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationParameter;
import org.testng.annotations.Test;

/**
 * Unit test for {@link GetAnnotationsParameter}.
 *
 * @author Gunnar Morling
 *
 */
public class GetAnnotationsParameterTest {

	@Test
	public void testGetMessageParameter() {
		NotNull testAnnotation = new NotNull() {
			@Override
			public String message() {
				return "test";
			}

			@Override
			public Class<?>[] groups() {
				return new Class<?>[] { Default.class };
			}

			@Override
			public Class<? extends Payload>[] payload() {
				@SuppressWarnings("unchecked")
				Class<? extends Payload>[] classes = new Class[] { };
				return classes;
			}

			@Override
			public Class<? extends Annotation> annotationType() {
				return this.getClass();
			}
		};
		String message = GetAnnotationParameter.action( testAnnotation, "message", String.class ).run();
		assertEquals( "test", message, "Wrong message" );

		Class<?>[] group = GetAnnotationParameter.action( testAnnotation, "groups", Class[].class ).run();
		assertEquals( group[0], Default.class, "Wrong message" );

		try {
			GetAnnotationParameter.action( testAnnotation, "message", Integer.class ).run();
			fail();
		}
		catch ( ValidationException e ) {
			assertTrue( e.getMessage().contains( "Wrong parameter type." ), "Wrong exception message" );
		}

		try {
			GetAnnotationParameter.action( testAnnotation, "foo", Integer.class ).run();
			fail();
		}
		catch ( ValidationException e ) {
			assertTrue(
					e.getMessage().contains( "The specified annotation defines no parameter" ),
					"Wrong exception message"
			);
		}
	}
}
