/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.traversableresolver;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.validation.Configuration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.TraversableResolver;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.constraints.ScriptAssert;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

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

		assertNoViolations( violations );
	}

	@ScriptAssert(lang = "groovy", script = "return true;")
	private static class Foo {
	}

	private static class Bar {
		@Valid
		private List<Foo> foos = Arrays.asList( new Foo(), new Foo() );
	}


	public static class ExceptionThrowingTraversableResolver implements TraversableResolver {
		@Override
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

		@Override
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




