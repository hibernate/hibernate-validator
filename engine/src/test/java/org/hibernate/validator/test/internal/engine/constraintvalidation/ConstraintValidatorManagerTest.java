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
package org.hibernate.validator.test.internal.engine.constraintvalidation;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.UnexpectedTypeException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.NotNullValidator;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;

import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorManagerTest {
	private ConstraintValidatorManager constraintValidatorManager;
	private ConstraintValidatorFactory constraintValidatorFactory;
	private Validator validator;

	@BeforeMethod
	public void setUp() {
		constraintValidatorFactory = new ConstraintValidatorFactoryImpl();
		constraintValidatorManager = new ConstraintValidatorManager( constraintValidatorFactory );
		validator = getValidator();
	}

	@Test
	public void testGetDefaultConstraintValidatorFactory() {
		assertTrue(
				constraintValidatorManager.getDefaultConstraintValidatorFactory() == constraintValidatorFactory,
				"Unexpected default factory"
		);
	}

	@Test
	public void testGetInitializedValidator() {
		ConstraintDescriptor<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

		ConstraintValidator<?, ?> constraintValidator = constraintValidatorManager.getInitializedValidator(
				String.class,
				constraintDescriptor,
				constraintValidatorFactory
		);

		assertTrue( constraintValidator instanceof NotNullValidator, "Unexpected validator type" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullValidatedValueThrowsIllegalArgumentException() {
		ConstraintDescriptor<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

		constraintValidatorManager.getInitializedValidator(
				null,
				constraintDescriptor,
				constraintValidatorFactory
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullDescriptorThrowsIllegalArgumentException() {
		constraintValidatorManager.getInitializedValidator(
				String.class,
				null,
				constraintValidatorFactory
		);
	}


	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullFactoryThrowsIllegalArgumentException() {
		ConstraintDescriptor<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

		constraintValidatorManager.getInitializedValidator(
				String.class,
				constraintDescriptor,
				null
		);
	}

	@Test(expectedExceptions = UnexpectedTypeException.class, expectedExceptionsMessageRegExp = "HV000030.*")
	public void testUnexpectedTypeException() {
		ConstraintDescriptor<?> constraintDescriptor = getConstraintDescriptorForProperty( "s2" );

		constraintValidatorManager.getInitializedValidator(
				Object.class,
				constraintDescriptor,
				constraintValidatorFactory
		);
	}

	@Test
	public void testConstraintValidatorInstancesAreCachedPerFactory() {
		ConstraintDescriptor<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

		ConstraintValidator<?, ?> constraintValidator1 = constraintValidatorManager.getInitializedValidator(
				String.class,
				constraintDescriptor,
				constraintValidatorFactory
		);

		assertTrue(
				constraintValidatorManager.numberOfCachedConstraintValidatorInstances() == 1,
				"There should be only one instance cached"
		);

		ConstraintValidator<?, ?> constraintValidator2 = constraintValidatorManager.getInitializedValidator(
				String.class,
				constraintDescriptor,
				new MyCustomValidatorFactory()
		);

		assertTrue(
				constraintValidatorManager.numberOfCachedConstraintValidatorInstances() == 2,
				"Constraint Validator Factory should be part of the cache key"
		);

		assertNotSame( constraintValidator1, constraintValidator2, "The validator instances should not be the same" );
	}

	@Test
	public void testOnlyTheInstancesForTheLeastRecentlyUsedCustomFactoryAreCached() {
		ConstraintDescriptor<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

		for ( int i = 0; i < 10; i++ ) {
			constraintValidatorManager.getInitializedValidator(
					String.class,
					constraintDescriptor,
					new MyCustomValidatorFactory()
			);

			assertEquals(
					constraintValidatorManager.numberOfCachedConstraintValidatorInstances(), 1,
					"There should be only one instance cached"
			);
		}

		constraintValidatorManager.clear();
		assertEquals(
				constraintValidatorManager.numberOfCachedConstraintValidatorInstances(),
				0,
				"Cache should be empty"
		);
	}

	private ConstraintDescriptor<?> getConstraintDescriptorForProperty(String propertyName) {
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( Foo.class );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty( propertyName );
		Set<ConstraintDescriptor<?>> constraintDescriptorSet = propertyDescriptor.getConstraintDescriptors();
		assertEquals( constraintDescriptorSet.size(), 1, "There should be only one constraint descriptor" );
		return constraintDescriptorSet.iterator().next();
	}

	public class Foo {
		@NotNull
		String s1;

		@Size
		String s2;
	}

	public class MyCustomValidatorFactory implements ConstraintValidatorFactory {
		private final ConstraintValidatorFactory delegate;

		public MyCustomValidatorFactory() {
			delegate = new ConstraintValidatorFactoryImpl();
		}

		@Override
		public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
			return delegate.getInstance( key );
		}

		@Override
		public void releaseInstance(ConstraintValidator<?, ?> instance) {
			delegate.releaseInstance( instance );
		}
	}

}


