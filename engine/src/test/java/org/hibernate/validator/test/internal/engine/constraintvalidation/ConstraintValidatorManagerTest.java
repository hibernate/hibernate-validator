/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.constraintvalidation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.getConstraintValidatorInitializationContext;
import static org.hibernate.validator.testutils.ConstraintValidatorInitializationHelper.getDummyConstraintValidatorInitializationContext;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.util.Set;

import jakarta.validation.ClockProvider;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.metadata.BeanDescriptor;
import jakarta.validation.metadata.ConstraintDescriptor;
import jakarta.validation.metadata.PropertyDescriptor;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.hibernate.validator.internal.engine.DefaultClockProvider;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManagerImpl;
import org.hibernate.validator.internal.engine.scripting.DefaultScriptEvaluatorFactory;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.spi.scripting.ScriptEvaluatorFactory;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class ConstraintValidatorManagerTest {
	private ConstraintValidatorManagerImpl constraintValidatorManager;
	private ConstraintValidatorFactory constraintValidatorFactory;
	private Validator validator;

	@BeforeMethod
	public void setUp() {
		constraintValidatorFactory = new ConstraintValidatorFactoryImpl();
		constraintValidatorManager = new ConstraintValidatorManagerImpl( constraintValidatorFactory, getDummyConstraintValidatorInitializationContext() );
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
				constraintValidatorFactory,
				getDummyConstraintValidatorInitializationContext()
		);

		assertTrue( constraintValidator instanceof NotNullValidator, "Unexpected validator type" );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullValidatedValueThrowsIllegalArgumentException() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

		constraintValidatorManager.getInitializedValidator(
				null,
				constraintDescriptor,
				constraintValidatorFactory,
				getDummyConstraintValidatorInitializationContext()
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullDescriptorThrowsIllegalArgumentException() {
		constraintValidatorManager.getInitializedValidator(
				String.class,
				null,
				constraintValidatorFactory,
				getDummyConstraintValidatorInitializationContext()
		);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullFactoryThrowsIllegalArgumentException() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

		constraintValidatorManager.getInitializedValidator(
				String.class,
				constraintDescriptor,
				null,
				getDummyConstraintValidatorInitializationContext()
		);
	}

	@Test
	public void testUnexpectedTypeException() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s2" );

		ConstraintValidator<?, ?> constraintValidator = constraintValidatorManager.getInitializedValidator(
				Object.class,
				constraintDescriptor,
				constraintValidatorFactory,
				getDummyConstraintValidatorInitializationContext()
		);
		assertNull( constraintValidator, "there should be no matching constraint validator" );
	}

	@Test
	public void testConstraintValidatorInstancesAreCachedPerFactory() {
		ConstraintDescriptorImpl<?> constraintDescriptor = getConstraintDescriptorForProperty( "s1" );

		ConstraintValidator<?, ?> constraintValidator1 = constraintValidatorManager.getInitializedValidator(
				String.class,
				constraintDescriptor,
				constraintValidatorFactory,
				getDummyConstraintValidatorInitializationContext()
		);

		assertTrue(
				constraintValidatorManager.numberOfCachedConstraintValidatorInstances() == 1,
				"There should be only one instance cached"
		);

		ConstraintValidator<?, ?> constraintValidator2 = constraintValidatorManager.getInitializedValidator(
				String.class,
				constraintDescriptor,
				new MyCustomValidatorFactory(),
				getDummyConstraintValidatorInitializationContext()
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
					new MyCustomValidatorFactory(),
					getDummyConstraintValidatorInitializationContext()
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

		ConstraintValidator<?, ?> notNullValidatorForFirstName1 = constraintValidatorManager.getInitializedValidator(
				String.class,
				notNullOnFirstNameDescriptor,
				constraintValidatorFactory,
				getDummyConstraintValidatorInitializationContext()
		);
		ConstraintValidator<?, ?> notNullValidatorForFirstName2 = constraintValidatorManager.getInitializedValidator(
				String.class,
				notNullOnFirstNameDescriptor,
				constraintValidatorFactory,
				getDummyConstraintValidatorInitializationContext()
		);
		ConstraintValidator<?, ?> notNullValidatorForLastName = constraintValidatorManager.getInitializedValidator(
				String.class,
				notNullOnLastNameDescriptor,
				constraintValidatorFactory,
				getDummyConstraintValidatorInitializationContext()
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

		ConstraintValidator<?, ?> sizeValidatorForMiddleName = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnMiddleNameDescriptor, constraintValidatorFactory, getDummyConstraintValidatorInitializationContext()
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress1 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress1Descriptor, constraintValidatorFactory, getDummyConstraintValidatorInitializationContext()
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress2 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress2Descriptor, constraintValidatorFactory, getDummyConstraintValidatorInitializationContext()
		);

		assertThat( sizeValidatorForMiddleName ).isNotSameAs( sizeValidatorForAddress1 );
		assertThat( sizeValidatorForAddress1 ).isSameAs( sizeValidatorForAddress2 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1589")
	public void testValidatorsAreCachedPerConstraintAndAnnotationMembersAndScriptEvaluatorFactory() {
		Validator validator = getConfiguration()
				.addMapping(
						ConstraintValidatorManagerTest.class.getResourceAsStream(
								"hv-1589-mapping.xml"
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

		ScriptEvaluatorFactory scriptEvaluatorFactory1 = new DefaultScriptEvaluatorFactory( null );
		ScriptEvaluatorFactory scriptEvaluatorFactory2 = new DefaultScriptEvaluatorFactory( null );

		HibernateConstraintValidatorInitializationContext initializationContext1 = getConstraintValidatorInitializationContext(
				scriptEvaluatorFactory1, DefaultClockProvider.INSTANCE, Duration.ZERO );
		HibernateConstraintValidatorInitializationContext initializationContext2 = getConstraintValidatorInitializationContext(
				scriptEvaluatorFactory2, DefaultClockProvider.INSTANCE, Duration.ZERO );

		ConstraintValidator<?, ?> sizeValidatorForMiddleNameCtx1 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnMiddleNameDescriptor, constraintValidatorFactory, initializationContext1
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress1Ctx1 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress1Descriptor, constraintValidatorFactory, initializationContext1
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress2Ctx1 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress2Descriptor, constraintValidatorFactory, initializationContext1
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress2Ctx2 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress2Descriptor, constraintValidatorFactory, initializationContext2
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress1Ctx2 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress1Descriptor, constraintValidatorFactory, initializationContext2
		);

		assertThat( sizeValidatorForMiddleNameCtx1 ).isNotSameAs( sizeValidatorForAddress1Ctx1 );
		assertThat( sizeValidatorForAddress1Ctx1 ).isSameAs( sizeValidatorForAddress2Ctx1 );
		assertThat( sizeValidatorForAddress1Ctx1 ).isNotSameAs( sizeValidatorForAddress2Ctx2 );
		assertThat( sizeValidatorForAddress2Ctx2 ).isSameAs( sizeValidatorForAddress1Ctx2 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1589")
	public void testValidatorsAreCachedPerConstraintAndAnnotationMembersAndClockProvider() {
		Validator validator = getConfiguration()
				.addMapping(
						ConstraintValidatorManagerTest.class.getResourceAsStream(
								"hv-1589-mapping.xml"
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

		ScriptEvaluatorFactory scriptEvaluatorFactory = new DefaultScriptEvaluatorFactory( null );

		ClockProvider clockProvider1 = new ClockProvider() {
			@Override
			public Clock getClock() {
				return null;
			}
		};
		ClockProvider clockProvider2 = new ClockProvider() {
			@Override
			public Clock getClock() {
				return null;
			}
		};

		HibernateConstraintValidatorInitializationContext initializationContext1 = getConstraintValidatorInitializationContext(
				scriptEvaluatorFactory, clockProvider1, Duration.ZERO );
		HibernateConstraintValidatorInitializationContext initializationContext2 = getConstraintValidatorInitializationContext(
				scriptEvaluatorFactory, clockProvider2, Duration.ZERO );

		ConstraintValidator<?, ?> sizeValidatorForMiddleNameCtx1 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnMiddleNameDescriptor, constraintValidatorFactory, initializationContext1
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress1Ctx1 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress1Descriptor, constraintValidatorFactory, initializationContext1
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress2Ctx1 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress2Descriptor, constraintValidatorFactory, initializationContext1
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress2Ctx2 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress2Descriptor, constraintValidatorFactory, initializationContext2
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress1Ctx2 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress1Descriptor, constraintValidatorFactory, initializationContext2
		);

		assertThat( sizeValidatorForMiddleNameCtx1 ).isNotSameAs( sizeValidatorForAddress1Ctx1 );
		assertThat( sizeValidatorForAddress1Ctx1 ).isSameAs( sizeValidatorForAddress2Ctx1 );
		assertThat( sizeValidatorForAddress1Ctx1 ).isNotSameAs( sizeValidatorForAddress2Ctx2 );
		assertThat( sizeValidatorForAddress2Ctx2 ).isSameAs( sizeValidatorForAddress1Ctx2 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1589")
	public void testValidatorsAreCachedPerConstraintAndAnnotationMembersAndTemporalValidationTolerance() {
		Validator validator = getConfiguration()
				.addMapping(
						ConstraintValidatorManagerTest.class.getResourceAsStream(
								"hv-1589-mapping.xml"
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

		ScriptEvaluatorFactory scriptEvaluatorFactory = new DefaultScriptEvaluatorFactory( null );

		HibernateConstraintValidatorInitializationContext initializationContext1 = getConstraintValidatorInitializationContext(
				scriptEvaluatorFactory, DefaultClockProvider.INSTANCE, Duration.ofDays( 1 ) );
		HibernateConstraintValidatorInitializationContext initializationContext2 = getConstraintValidatorInitializationContext(
				scriptEvaluatorFactory, DefaultClockProvider.INSTANCE, Duration.ofDays( 999 ) );

		ConstraintValidator<?, ?> sizeValidatorForMiddleNameCtx1 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnMiddleNameDescriptor, constraintValidatorFactory, initializationContext1
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress1Ctx1 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress1Descriptor, constraintValidatorFactory, initializationContext1
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress2Ctx1 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress2Descriptor, constraintValidatorFactory, initializationContext1
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress2Ctx2 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress2Descriptor, constraintValidatorFactory, initializationContext2
		);
		ConstraintValidator<?, ?> sizeValidatorForAddress1Ctx2 = constraintValidatorManager.getInitializedValidator(
				String.class, sizeOnAddress1Descriptor, constraintValidatorFactory, initializationContext2
		);

		assertThat( sizeValidatorForMiddleNameCtx1 ).isNotSameAs( sizeValidatorForAddress1Ctx1 );
		assertThat( sizeValidatorForAddress1Ctx1 ).isSameAs( sizeValidatorForAddress2Ctx1 );
		assertThat( sizeValidatorForAddress1Ctx1 ).isNotSameAs( sizeValidatorForAddress2Ctx2 );
		assertThat( sizeValidatorForAddress2Ctx2 ).isSameAs( sizeValidatorForAddress1Ctx2 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1589")
	public void testHibernateConstraintValidatorsAreNotCachedByConstraintTree() {
		try ( ValidatorFactory factory = getConfiguration().buildValidatorFactory() ) {

			// Let's validate with different initialization contexts to make sure the ConstraintTree instance
			// doesn't cache a HibernateConstraintValidator instance

			assertThat( factory
					.getValidator().validate( new Company() ).iterator().next().getMessage() ).isEqualTo( "PT0S" );

			assertThat( factory
					.unwrap( HibernateValidatorFactory.class ).usingContext()
					.temporalValidationTolerance( Duration.ofHours( 7 ) )
					.getValidator().validate( new Company() ).iterator().next().getMessage() ).isEqualTo( "PT7H" );

			assertThat( factory
					.unwrap( HibernateValidatorFactory.class ).usingContext()
					.temporalValidationTolerance( Duration.ofDays( 5 ) )
					.getValidator().validate( new Company() ).iterator().next().getMessage() ).isEqualTo( "PT120H" );
		}
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
