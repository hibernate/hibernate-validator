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
package org.hibernate.validator.test.internal.engine.failfast;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.fail;

/**
 * Tests for fail fast mode
 *
 * @author Emmanuel Bernard
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 * @author Hardy Ferentschik
 */
public class FailFastTest {
	private static final Log log = LoggerFactory.make();
	private final A testInstance = new A();

	@Test
	@TestForIssue(jiraKey = "HV-381")
	public void testFailFastDefaultBehaviour() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.buildValidatorFactory();

		final Validator validator = factory.getValidator();

		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 2 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-381")
	public void testFailFastMethodValidationDefaultBehaviour() {
		TestService service = getValidatingProxy( new TestServiceImpl(), ValidatorUtil.getValidator() );

		try {
			service.testMethod( " ", null );
			fail();
		}
		catch ( ConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 3 );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-381")
	public void testFailFastSetOnConfiguration() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.failFast( true ).buildValidatorFactory();

		final Validator validator = factory.getValidator();
		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-381")
	public void testFailFastMethodValidationOnConfiguration() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.failFast( true ).buildValidatorFactory();

		final Validator validator = factory.getValidator();

		TestService service = getValidatingProxy( new TestServiceImpl(), validator );

		try {
			service.testMethod( "a", null );
			fail();
		}
		catch ( ConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 1 );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-381")
	public void testFailFastSetOnValidatorFactory() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.buildValidatorFactory();

		final Validator validator =
				factory.unwrap( HibernateValidatorFactory.class )
						.usingContext()
						.failFast( true )
						.getValidator();
		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-381")
	public void testFailFastMethodValidationSetOnValidatorFactory() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.buildValidatorFactory();

		final Validator validator =
				factory.unwrap( HibernateValidatorFactory.class )
						.usingContext()
						.failFast( true )
						.getValidator();

		TestService service = getValidatingProxy( new TestServiceImpl(), validator );

		try {
			service.testMethod( " ", null );
			fail();
		}
		catch ( ConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 1 );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-381")
	public void testFailFastSetWithProperty() {
		// with fail fast
		HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ValidatorFactory factory = configuration.addProperty( HibernateValidatorConfiguration.FAIL_FAST, "true" )
				.buildValidatorFactory();

		Validator validator = factory.getValidator();

		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 1 );

		// without fail fast
		configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		factory = configuration.addProperty( HibernateValidatorConfiguration.FAIL_FAST, "false" )
				.buildValidatorFactory();

		validator = factory.getValidator();
		constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 2 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-381")
	public void testFailFastMethodValidationSetWithProperty() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.addProperty( HibernateValidatorConfiguration.FAIL_FAST, "true" )
				.buildValidatorFactory();

		final Validator validator = factory.getValidator();

		TestService service = getValidatingProxy( new TestServiceImpl(), validator );

		try {
			service.testMethod( " ", null );
			fail();
		}
		catch ( ConstraintViolationException e ) {
			assertNumberOfViolations( e.getConstraintViolations(), 1 );
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-381")
	public void testFailFastSetWithInvalidProperty() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );

		//Default fail fast property value is false
		final ValidatorFactory factory = configuration.addProperty(
				HibernateValidatorConfiguration.FAIL_FAST, "not correct"
		).buildValidatorFactory();

		final Validator validator = factory.getValidator();
		Set<ConstraintViolation<A>> constraintViolations = validator.validate( testInstance );
		assertNumberOfViolations( constraintViolations, 2 );
	}

	@Test(expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV[0-9]*: Inconsistent fail fast configuration.*")
	@TestForIssue(jiraKey = "HV-381")
	public void testFailFastSetWithInconsistentConfiguration() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );

		//Default fail fast property value is false
		final ValidatorFactory factory = configuration.addProperty(
				HibernateValidatorConfiguration.FAIL_FAST, "false"
		).failFast( true ).buildValidatorFactory();

		factory.getValidator();
	}

	@Test
	@TestForIssue(jiraKey = "HV-550")
	public void testFailFastComposingConstraints() {
		final HibernateValidatorConfiguration configuration = ValidatorUtil.getConfiguration( HibernateValidator.class );
		final ValidatorFactory factory = configuration.failFast( true ).buildValidatorFactory();

		final Validator validator = factory.getValidator();
		Set<ConstraintViolation<FooBar>> constraintViolations = validator.validate( new FooBar() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintViolationMessages( constraintViolations, "Bar constraint failed!" );
	}

	public void testFailSafePerformance() {
		final Validator regularValidator = ValidatorUtil.getConfiguration().buildValidatorFactory().getValidator();
		final Validator failFastValidator = ValidatorUtil.getConfiguration()
				.buildValidatorFactory()
				.unwrap( HibernateValidatorFactory.class )
				.usingContext()
				.failFast( true )
				.getValidator();

		final int loopTime = 50000;
		for ( int i = 0; i < loopTime; i++ ) {
			validateBatch( regularValidator );
		}

		for ( int i = 0; i < loopTime; i++ ) {
			validateBatch( failFastValidator );
		}

		long start = System.nanoTime();
		for ( int i = 0; i < loopTime; i++ ) {
			validateBatch( regularValidator );
		}
		long timeOfRegular = System.nanoTime() - start;

		start = System.nanoTime();
		for ( int i = 0; i < loopTime; i++ ) {
			validateBatch( failFastValidator );
		}
		long timeOfFailFast = System.nanoTime() - start;

		log.debugf( "Regular = %d\n FailFast: %d", timeOfRegular, timeOfFailFast );
	}

	private void validateBatch(Validator validator) {
		validator.validate( buildA() );
	}

	static int i = 0;

	A buildA() {
		A a = new A();
		a.b = "bbb" + i++;
		a.file = "test" + i++ + ".txt";
		a.bs.add( buildB() );
		a.bs.add( buildB() );
		a.bs.add( buildB() );
		a.bs.add( buildB() );
		return a;
	}

	B buildB() {
		B b = new B();
		b.size = 45 + i++;
		return b;
	}

	class A {
		@NotNull
		String b;

		@NotNull
		@Email
		String c;

		@Pattern(regexp = ".*\\.txt$")
		String file;

		@Valid
		Set<B> bs = new HashSet<B>();
	}

	class B {
		@Min(value = 10)
		@Max(value = 30)
		@NotNull
		Integer size;
	}

	interface TestService {
		void testMethod(@Min(2) @NotBlank String param1, @NotNull String param2);
	}

	class TestServiceImpl implements TestService {
		@Override
		public void testMethod(String param1, String param2) {
		}
	}

	@FooConstraint
	public class FooBar {
	}

	@BarConstraint(message = "Bar constraint failed!")
	@Target({ TYPE })
	@Retention(RUNTIME)
	@Constraint(validatedBy = { FooConstraintValidator.class })
	public @interface FooConstraint {
		String message() default "invalid name";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	@Target({ ANNOTATION_TYPE })
	@Retention(RUNTIME)
	@Constraint(validatedBy = { BarConstraintValidator.class })
	public @interface BarConstraint {
		String message() default "invalid name";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };
	}

	public static class BarConstraintValidator implements ConstraintValidator<BarConstraint, FooBar> {

		@Override
		public void initialize(BarConstraint constraintAnnotation) {
		}

		@Override
		public boolean isValid(FooBar value, ConstraintValidatorContext context) {
			return false;
		}
	}

	public static class FooConstraintValidator implements ConstraintValidator<FooConstraint, FooBar> {

		@Override
		public void initialize(FooConstraint constraintAnnotation) {
		}

		@Override
		public boolean isValid(FooBar value, ConstraintValidatorContext context) {
			throw new RuntimeException( "Should not be executed due to fail fast mode" );
		}
	}
}
