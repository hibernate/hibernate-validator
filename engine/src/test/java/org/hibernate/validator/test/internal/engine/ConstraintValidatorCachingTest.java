/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getConfiguration;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Configuration;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.hibernate.validator.internal.constraintvalidators.bv.number.bound.MinValidatorForNumber;
import org.hibernate.validator.internal.constraintvalidators.bv.size.SizeValidatorForCollection;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorFactoryImpl;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorCachingTest {

	@Test
	@TestForIssue(jiraKey = "HV-564")
	public void testConstraintValidatorInstancesAreCached() {
		Configuration<?> config = getConfiguration();
		OnceInstanceOnlyConstraintValidatorFactory constraintValidatorFactory = new OnceInstanceOnlyConstraintValidatorFactory();

		config.constraintValidatorFactory( constraintValidatorFactory );
		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Person john = new Person( "John Doe" );
		john.setAge( 36 );
		john.addAddress( new Address( "Mysterious Lane", "Mysterious" ) );

		constraintValidatorFactory.assertSize( 0 );
		Set<ConstraintViolation<Person>> violations = validator.validate( john );
		assertNoViolations( violations );
		constraintValidatorFactory.assertSize( 3 );

		// need to call validate twice to let the cache kick in
		violations = validator.validate( john );
		assertNoViolations( violations );
		constraintValidatorFactory.assertSize( 3 );

		constraintValidatorFactory.assertKeyExists( SizeValidatorForCollection.class );
		constraintValidatorFactory.assertKeyExists( MinValidatorForNumber.class );
		constraintValidatorFactory.assertKeyExists( NotNullValidator.class );

		// getting a new validator from the same factory should have the same instances cached
		validator = factory.getValidator();
		constraintValidatorFactory.assertSize( 3 );

		violations = validator.validate( john );
		assertNoViolations( violations );
		constraintValidatorFactory.assertSize( 3 );

		factory.close();
		constraintValidatorFactory.assertAllConstraintValidatorInstancesReleased();
	}

	@Test
	@TestForIssue(jiraKey = "HV-564")
	public void testConstraintValidatorInstancesAreCachedPerConstraintValidatorFactory() {
		Configuration<?> config = getConfiguration();
		OnceInstanceOnlyConstraintValidatorFactory constraintValidatorFactory1 = new OnceInstanceOnlyConstraintValidatorFactory();
		OnceInstanceOnlyConstraintValidatorFactory constraintValidatorFactory2 = new OnceInstanceOnlyConstraintValidatorFactory();

		config.constraintValidatorFactory( constraintValidatorFactory1 );
		ValidatorFactory factory = config.buildValidatorFactory();
		Validator validator = factory.getValidator();

		Person john = new Person( "John Doe" );
		john.setAge( 36 );
		john.addAddress( new Address( "Mysterious Lane", "Mysterious" ) );

		constraintValidatorFactory1.assertSize( 0 );
		Set<ConstraintViolation<Person>> violations = validator.validate( john );
		assertNoViolations( violations );
		constraintValidatorFactory1.assertSize( 3 );

		// getting a new validator with a new constraint factory
		validator = factory.usingContext().constraintValidatorFactory( constraintValidatorFactory2 ).getValidator();
		constraintValidatorFactory2.assertSize( 0 );
		violations = validator.validate( john );
		assertNoViolations( violations );
		constraintValidatorFactory2.assertSize( 3 );
		constraintValidatorFactory1.assertConstraintValidatorInstancesAreNotShared( constraintValidatorFactory2 );

		factory.close();
		constraintValidatorFactory1.assertAllConstraintValidatorInstancesReleased();
		constraintValidatorFactory2.assertAllConstraintValidatorInstancesReleased();
	}

	@Test
	@TestForIssue(jiraKey = "HV-243")
	public void testConstraintValidatorInstancesAreCachedPerConstraint() {
		Validator validator = getValidator();
		Set<ConstraintViolation<Foo>> violations = validator.validate( new Foo() );
		assertThat( violations ).containsOnlyViolations(
				violationOf( Size.class ).withMessage( "size 10" ),
				violationOf( Size.class ).withMessage( "size 20" )
		);
	}

	public class OnceInstanceOnlyConstraintValidatorFactory implements ConstraintValidatorFactory {
		ConstraintValidatorFactoryImpl factory = new ConstraintValidatorFactoryImpl();
		Map<Class<?>, ConstraintValidator<?, ?>> instantiatedConstraintValidatorClasses = newHashMap();

		OnceInstanceOnlyConstraintValidatorFactory() {
		}

		@Override
		public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
			T constraintValidator = factory.getInstance( key );
			if ( instantiatedConstraintValidatorClasses.containsKey( key ) ) {
				if ( instantiatedConstraintValidatorClasses.get( key ) != constraintValidator ) {
					fail( "The ConstraintValidator instance should be cached: " + constraintValidator );
				}
			}
			else {
				instantiatedConstraintValidatorClasses.put( key, constraintValidator );
			}

			return constraintValidator;
		}

		public void assertKeyExists(Class<?> clazz) {
			assertTrue(
					instantiatedConstraintValidatorClasses.containsKey( clazz ),
					"An constraint validator of type " + clazz.getName() + " should exist"
			);
		}

		public void assertSize(int size) {
			assertEquals(
					instantiatedConstraintValidatorClasses.size(),
					size,
					"Wrong number of already cached constraint validator instances"
			);
		}

		public void assertConstraintValidatorInstancesAreNotShared(OnceInstanceOnlyConstraintValidatorFactory otherFactory) {
			for ( ConstraintValidator<?, ?> constraintValidator : instantiatedConstraintValidatorClasses.values() ) {
				if ( otherFactory.instantiatedConstraintValidatorClasses.containsValue( constraintValidator ) ) {
					fail( "The two constraint validator factories should not share any ConstraintValidator instances" );
				}
			}
		}

		public void assertAllConstraintValidatorInstancesReleased() {
			assertTrue(
					instantiatedConstraintValidatorClasses.isEmpty(),
					"All validator instances should have been released"
			);
		}

		@Override
		public void releaseInstance(ConstraintValidator<?, ?> instance) {
			Class<?> key = null;
			for ( Map.Entry<Class<?>, ConstraintValidator<?, ?>> entry : instantiatedConstraintValidatorClasses.entrySet() ) {
				if ( entry.getValue() == instance ) {
					key = entry.getKey();
				}
			}
			instantiatedConstraintValidatorClasses.remove( key );
		}
	}

	class Person {
		@NotNull
		String name;

		@Min(1)
		int age;

		@Size(min = 1, max = 3)
		List<Address> addresses;

		Person(String name) {
			this.name = name;
			this.addresses = new ArrayList<Address>();
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public void addAddress(Address address) {
			addresses.add( address );
		}


		public List<Address> getAddresses() {
			return addresses;
		}
	}

	class Address {
		String street;
		String city;

		Address(String street, String city) {
			this.street = street;
			this.city = city;
		}
	}

	@Size(max = 20, message = "size 20")
	@Constraint(validatedBy = { })
	@Target({ METHOD, FIELD, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	public @interface A {
		String message() default "A";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	@Size(max = 10, message = "size 10")
	@Constraint(validatedBy = { })
	@Target({ METHOD, FIELD, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	public @interface B {
		String message() default "B";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	@A
	@B
	@Constraint(validatedBy = { })
	@Target({ METHOD, FIELD, ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Documented
	public @interface C {
		String message() default "C";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	class Foo {
		@C
		String value = "The quick brown fox jumps over the lazy dog";
	}
}


