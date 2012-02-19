/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.traversableresolver;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Path;
import javax.validation.TraversableResolver;
import javax.validation.Valid;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

/**
 * @author Hardy Ferentschik
 */
public class TraversableResolverTest {

	@Test
	@TestForIssue(jiraKey = "HV-524")
	public void testClassConstraintsAreNotCallingTraversableResolver() {
		Configuration<HibernateValidatorConfiguration> config = ValidatorUtil.getConfiguration();
		config.traversableResolver( new ExceptionThrowingTraversableResolver() );
		Validator validator = config.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<Bar>> violations = validator.validate( new Bar() );

		assertNumberOfViolations( violations, 0 );
	}

	@ScriptAssert(lang = "groovy", script = "return true;")
	private static class Foo {
	}

	private static class Bar {
		@Valid
		private List<Foo> foos = Arrays.asList( new Foo(), new Foo() );
	}


	public static class ExceptionThrowingTraversableResolver implements TraversableResolver {
		public boolean isReachable(Object traversableObject,
								   Path.Node traversableProperty,
								   Class<?> rootBeanType,
								   Path pathToTraversableObject,
								   ElementType elementType) {

			if ( ElementType.TYPE.equals( elementType ) ) {
				throw new IllegalArgumentException( "ElementType.TYPE is not allowed as argument type" );
			}

			return true;
		}

		public boolean isCascadable(Object traversableObject,
									Path.Node traversableProperty,
									Class<?> rootBeanType,
									Path pathToTraversableObject,
									ElementType elementType) {

			if ( ElementType.TYPE.equals( elementType ) ) {
				throw new IllegalArgumentException( "ElementType.TYPE is not allowed as argument type" );
			}

			return true;
		}
	}
}




