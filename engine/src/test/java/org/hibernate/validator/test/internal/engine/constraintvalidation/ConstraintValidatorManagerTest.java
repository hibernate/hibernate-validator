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
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.testutil.TestForIssue;

import static org.fest.assertions.Assertions.assertThat;
import static org.hibernate.validator.testutil.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
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
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

		ConstraintValidator<?, ?> constraintValidator = constraintValidatorManager.getInitializedValidator(
				String.class,
				constraintDescriptor,
				constraintValidatorFactory
		);

		assertTrue( constraintValidator instanceof NotNullValidator, "Unexpected validator type" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullValidatedValueThrowsIllegalArgumentException() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

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
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

		constraintValidatorManager.getInitializedValidator(
				String.class,
				constraintDescriptor,
				null
		);
	}

	@Test(expectedExceptions = UnexpectedTypeException.class,
			expectedExceptionsMessageRegExp = "HV000030.*")
	public void testUnexpectedTypeException() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s2" );

		constraintValidatorManager.getInitializedValidator(
				Object.class,
				constraintDescriptor,
				constraintValidatorFactory
		);
	}

	@Test
	public void testConstraintValidatorInstancesAreCachedPerFactory() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

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

		assertNotSame(
				constraintValidator1,
				constraintValidator2,
				"The validator instances should not be the same"
		);
	}

	@Test
	public void testOnlyTheInstancesForTheLeastRecentlyUsedCustomFactoryAreCached() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

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

	@Test
	@TestForIssue(jiraKey = "HV-662")
	public void testValidatorsAreCachedPerConstraint() {
		Validator validator = getConfiguration()
				.addMapping(
						ConstraintValidatorManagerTest.class.getResourceAsStream(
								"hv-662-mapping.xml"
						)
				)
				.buildValidatorFactory()
				.getValidator();

		ConstraintDescriptorImpl<?> notNullOnFirstNameDescriptor = getSingleConstraintDescriptorForProperty(
				validator, User.class, "firstName"
		);
		ConstraintDescriptorImpl<?> notNullOnLastNameDescriptor = getSingleConstraintDescriptorForProperty(
				validator, User.class, "lastName"
		);

		ConstraintValidator<?, Object> notNullValidatorForFirstName1 = constraintValidatorManager.getInitializedValidator(
				String.class, notNullOnFirstNameDescriptor, constraintValidatorFactory
		);
		ConstraintValidator<?, Object> notNullValidatorForFirstName2 = constraintValidatorManager.getInitializedValidator(
				String.class, notNullOnFirstNameDescriptor, constraintValidatorFactory
		);
		ConstraintValidator<?, Object> notNullValidatorForLastName = constraintValidatorManager.getInitializedValidator(
				String.class, notNullOnLastNameDescriptor, constraintValidatorFactory
		);

		assertThat( notNullValidatorForFirstName1 ).isSameAs( notNullValidatorForFirstName2 );
		assertThat( notNullValidatorForFirstName1 ).isSameAs( notNullValidatorForLastName );
	}

	@Test
	@TestForIssue(jiraKey = "HV-662")
	public void testValidatorsAreCachedPerConstraintAndAnnotationMembers() {
		Validator validator = getConfiguration()
				.addMapping(
						ConstraintValidatorManagerTest.class.getResourceAsStream(
								"hv-662-mapping.xml"
						)
				)
				.buildValidatorFactory()
				.getValidator();

		ConstraintDescriptorImpl<?> sizeOnMiddleNameDescriptor = getSingleConstraintDescriptorForProperty(
				validator, User.class, "middleName"
		);
		ConstraintDescriptorImpl<?> sizeOnAddress1Descriptor = getSingleConstraintDescriptorForProperty(
				validator, User.class, "address1"
		);
		ConstraintDescriptorImpl<?> sizeOnAddress2Descriptor = getSingleConstraintDescriptorForProperty(
				validator, User.class, "address2"
		);

		ConstraintValidator<?, Object> sizeValidatorForMiddleName = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnMiddleNameDescriptor, constraintValidatorFactory
		);
		ConstraintValidator<?, Object> sizeValidatorForAddress1 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress1Descriptor, constraintValidatorFactory
		);
		ConstraintValidator<?, Object> sizeValidatorForAddress2 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress2Descriptor, constraintValidatorFactory
		);

		assertThat( sizeValidatorForMiddleName ).isNotSameAs( sizeValidatorForAddress1 );
		assertThat( sizeValidatorForAddress1 ).isSameAs( sizeValidatorForAddress2 );
	}

	private ConstraintDescriptorImpl<?> getConstraintDescriptorForProperty(String propertyName) {
		return getSingleConstraintDescriptorForProperty( validator, Foo.class, propertyName );
	}

	private ConstraintDescriptorImpl<?> getSingleConstraintDescriptorForProperty(Validator validator, Class<?> clazz, String propertyName) {
		BeanDescriptor beanDescriptor = validator.getConstraintsForClass( clazz );
		PropertyDescriptor propertyDescriptor = beanDescriptor.getConstraintsForProperty(
				propertyName
		);
		Set<ConstraintDescriptor<?>> constraintDescriptorSet = propertyDescriptor.getConstraintDescriptors();
		assertEquals(
				constraintDescriptorSet.size(),
				1,
				"There should be only one constraint descriptor"
		);
		return (ConstraintDescriptorImpl<?>) constraintDescriptorSet.iterator().next();
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
