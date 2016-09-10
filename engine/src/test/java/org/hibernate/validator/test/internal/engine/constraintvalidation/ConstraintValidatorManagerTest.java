/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.constraintvalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.hibernate.validator.internal.engine.ValueContext;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.testutil.TestForIssue;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
@SuppressWarnings("unchecked")
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
		ValueContext valueContext = ValueContext.getLocalExecutionContext( new Foo(), null, null );
		valueContext.setDeclaredTypeOfValidatedElement( String.class );

		ConstraintValidator<?, ?> constraintValidator = constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(),
				constraintDescriptor,
				constraintValidatorFactory
		);

		assertTrue( constraintValidator instanceof NotNullValidator, "Unexpected validator type" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullValidatedValueThrowsIllegalArgumentException() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );
		ValueContext valueContext = ValueContext.getLocalExecutionContext( new Foo(), null, null );
		valueContext.setDeclaredTypeOfValidatedElement( null );

		constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(),
				constraintDescriptor,
				constraintValidatorFactory
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullDescriptorThrowsIllegalArgumentException() {
		ValueContext valueContext = ValueContext.getLocalExecutionContext( new Foo(), null, null );
		valueContext.setDeclaredTypeOfValidatedElement( String.class );

		constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(),
				null,
				constraintValidatorFactory
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullFactoryThrowsIllegalArgumentException() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );
		ValueContext valueContext = ValueContext.getLocalExecutionContext( new Foo(), null, null );
		valueContext.setDeclaredTypeOfValidatedElement( String.class );

		constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(),
				constraintDescriptor,
				null
		);
	}

	@Test
	public void testUnexpectedTypeException() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s2" );

		ValueContext valueContext = ValueContext.getLocalExecutionContext(
				new Foo(), null, PathImpl.createPathFromString( "s2" )
		);
		valueContext.setDeclaredTypeOfValidatedElement( Object.class );

		ConstraintValidator<?, ?> constraintValidator = constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(),
				constraintDescriptor,
				constraintValidatorFactory
		);
		assertNull( constraintValidator, "there should be no matching constraint validator" );
	}

	@Test
	public void testConstraintValidatorInstancesAreCachedPerFactory() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );
		ValueContext valueContext = ValueContext.getLocalExecutionContext( new Foo(), null, null );
		valueContext.setDeclaredTypeOfValidatedElement( String.class );

		ConstraintValidator<?, ?> constraintValidator1 = constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(),
				constraintDescriptor,
				constraintValidatorFactory
		);

		assertTrue(
				constraintValidatorManager.numberOfCachedConstraintValidatorInstances() == 1,
				"There should be only one instance cached"
		);

		ConstraintValidator<?, ?> constraintValidator2 = constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(),
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
		ValueContext valueContext = ValueContext.getLocalExecutionContext( new Foo(), null, null );
		valueContext.setDeclaredTypeOfValidatedElement( String.class );

		for ( int i = 0; i < 10; i++ ) {
			constraintValidatorManager.getInitializedValidator(
					valueContext.getDeclaredTypeOfValidatedElement(),
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

		ValueContext valueContext = ValueContext.getLocalExecutionContext( new User(), null, null );
		valueContext.setDeclaredTypeOfValidatedElement( String.class );

		ConstraintDescriptorImpl<?> notNullOnFirstNameDescriptor = getSingleConstraintDescriptorForProperty(
				validator, User.class, "firstName"
		);
		ConstraintDescriptorImpl<?> notNullOnLastNameDescriptor = getSingleConstraintDescriptorForProperty(
				validator, User.class, "lastName"
		);

		ConstraintValidator<?, Object> notNullValidatorForFirstName1 = constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(),
				notNullOnFirstNameDescriptor,
				constraintValidatorFactory
		);
		ConstraintValidator<?, Object> notNullValidatorForFirstName2 = constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(),
				notNullOnFirstNameDescriptor,
				constraintValidatorFactory
		);
		ConstraintValidator<?, Object> notNullValidatorForLastName = constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(),
				notNullOnLastNameDescriptor,
				constraintValidatorFactory
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

		ValueContext valueContext = ValueContext.getLocalExecutionContext( new User(), null, null );
		valueContext.setDeclaredTypeOfValidatedElement( String.class );

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
				valueContext.getDeclaredTypeOfValidatedElement(), sizeOnMiddleNameDescriptor, constraintValidatorFactory
		);
		ConstraintValidator<?, Object> sizeValidatorForAddress1 = constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(), sizeOnAddress1Descriptor, constraintValidatorFactory
		);
		ConstraintValidator<?, Object> sizeValidatorForAddress2 = constraintValidatorManager.getInitializedValidator(
				valueContext.getDeclaredTypeOfValidatedElement(), sizeOnAddress2Descriptor, constraintValidatorFactory
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
