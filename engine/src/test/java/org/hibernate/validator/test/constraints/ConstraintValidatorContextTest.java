/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.constraints;

import java.lang.annotation.Retention;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;
import javax.validation.executable.ExecutableValidator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.internal.util.CollectionHelper.asSet;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;

/**
 * Tests for the {@link javax.validation.ConstraintValidatorContext} API.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorContextTest {

	private Validator validator;
	private ExecutableValidator executableValidator;

	@BeforeMethod
	public void setUpValidators() {
		validator = ValidatorUtil.getValidator();
		executableValidator = validator.forExecutables();
	}

	@Test
	@TestForIssue(jiraKey = "HV-198")
	public void testCorrectSubNodePath() {
		Item item = new Item();
		item.interval = new Interval();
		item.interval.start = 10;
		item.interval.end = 5;

		Set<ConstraintViolation<Item>> constraintViolations = validator.validate( item );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "interval.start" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-208")
	public void testCorrectPath() {
		Item item = new Item();
		Interval interval = new Interval();
		item.interval = interval;
		item.interval.start = 10;
		item.interval.end = 5;

		Set<ConstraintViolation<Interval>> constraintViolations = validator.validate( interval );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "start" );
	}

	@Test
	public void testLegacyAddNode() {
		Set<ConstraintViolation<MyObject>> constraintViolations = validator.validate( new MyObject() );

		assertNumberOfViolations( constraintViolations, 4 );
		assertCorrectPropertyPaths( constraintViolations, "field1", "field2", "myNode1", "myNode2[key].myNode3" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-709")
	public void testAddPropertyNode() {
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );

		//violated constraint is class-level, thus the paths start with the first added sub-node
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith().property( "myNode1" ),
				pathWith().property( "myNode2" ).property( "myNode3" ),
				pathWith().property( "myNode4" ).property( "myNode5", true, null, null ),
				pathWith().property( "myNode6" ).property( "myNode7", true, null, 42 ),
				pathWith().property( "myNode8" ).property( "myNode9", true, "Foo", null ),
				pathWith().property( "myNode10" ).property( "myNode11", true, null, null ).property( "myNode12" )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-709")
	public void testAddBeanNode() {
		Set<ConstraintViolation<User>> constraintViolations = validator.validate( new User() );

		//violated constraint is property-level, thus the paths start with that property
		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith().property( "address" ).bean(),
				pathWith().property( "address" ).property( "myNode1" ).bean(),
				pathWith().property( "address" ).property( "myNode2", true, null, null ).bean(),
				pathWith().property( "address" ).property( "myNode3", true, null, 84 ).bean(),
				pathWith().property( "address" ).property( "myNode4", true, "AnotherKey", null ).bean(),
				pathWith().property( "address" ).bean( true, null, null ),
				pathWith().property( "address" ).bean( true, null, 42 ),
				pathWith().property( "address" ).bean( true, "Key", null )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-709")
	public void testAddingNodesInClassLevelConstraintKeepsInIterableKeyAndIndex() {
		Set<ConstraintViolation<FooContainer>> constraintViolations = validator.validate( new FooContainer() );

		assertThat( constraintViolations ).containsPaths(
				pathWith().property( "fooList" ).property( "myNode1", true, null, 1 ),
				pathWith().property( "fooArray" ).property( "myNode1", true, null, 1 ),
				pathWith().property( "fooSet" ).property( "myNode1", true, null, null ),
				pathWith().property( "fooMap" ).property( "myNode1", true, "MapKey", null )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-709")
	public void testAddParameterNode() throws Exception {
		Set<ConstraintViolation<User>> constraintViolations = executableValidator.validateParameters(
				new User(),
				User.class.getMethod( "setAddresses", Map.class ),
				new java.lang.Object[] { new HashMap() }
		);

		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith().method( "setAddresses" ).parameter( "arg0", 0 ),
				pathWith().method( "setAddresses" ).parameter( "arg0", 0 ).bean(),
				pathWith().method( "setAddresses" )
						.parameter( "arg0", 0 )
						.property( "myNode1", true, null, 23 )
						.bean()
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-709")
	public void testAddParameterNodeUsingCustomParameterNameProvider() throws Exception {
		ExecutableValidator executableValidator = ValidatorUtil.getConfiguration()
				.parameterNameProvider( new MyParameterNameProvider() )
				.buildValidatorFactory()
				.getValidator()
				.forExecutables();

		Set<ConstraintViolation<User>> constraintViolations = executableValidator.validateParameters(
				new User(),
				User.class.getMethod( "setAddresses", Map.class ),
				new java.lang.Object[] { new HashMap() }
		);


		assertThat( constraintViolations ).containsOnlyPaths(
				pathWith().method( "setAddresses" ).parameter( "param0", 0 ),
				pathWith().method( "setAddresses" ).parameter( "param0", 0 ).bean(),
				pathWith().method( "setAddresses" )
						.parameter( "param0", 0 )
						.property( "myNode1", true, null, 23 )
						.bean()
		);
	}

	@Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "HV000146.*")
	@TestForIssue(jiraKey = "HV-709")
	public void testAddParameterNodeForFieldLevelConstraintCausesException() throws Throwable {
		try {
			validator.validate( new Bar() );
		}
		catch ( ValidationException e ) {
			throw e.getCause();
		}
	}

	@MyClassLevelValidation
	private static class MyObject {
		@NotNull
		String field1;

		@NotNull
		String field2;
	}

	@ClassLevelValidationAddingPropertyNodes
	private static class Foo {
	}

	private static class FooContainer {
		@Valid
		private final List<Foo> fooList = Arrays.asList( null, new Foo() );

		@Valid
		private final Foo[] fooArray = new Foo[] { null, new Foo() };

		@Valid
		private final Set<Foo> fooSet = asSet( null, new Foo() );

		@Valid
		private final Map<String, Foo> fooMap;

		public FooContainer() {
			fooMap = newHashMap();
			fooMap.put( "MapKey", new Foo() );
		}

	}

	private static class Bar {

		@FieldLevelValidationAddingParameterNode
		private String bar;
	}

	private static class User {
		@PropertyLevelValidationAddingBeanAndPropertyNodes
		public Address getAddress() {
			return null;
		}

		@CrossParameterValidationAddingParameterBeanAndPropertyNodes
		public void setAddresses(Map<String, Address> addresses) {

		}
	}

	private static class Address {
		@SuppressWarnings("unused")
		public String getStreet() {
			return null;
		}

		@SuppressWarnings("unused")
		public Country getCountry() {
			return null;
		}
	}

	private static class Country {
		@SuppressWarnings("unused")
		public String getName() {
			return null;
		}
	}

	@Retention(RUNTIME)
	@Constraint(validatedBy = MyClassLevelValidation.Validator.class)
	public @interface MyClassLevelValidation {
		String message() default "failed";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		public static class Validator implements ConstraintValidator<MyClassLevelValidation, MyObject> {
			@Override
			public void initialize(MyClassLevelValidation constraintAnnotation) {
			}

			@SuppressWarnings("deprecation")
			@Override
			public boolean isValid(MyObject value, ConstraintValidatorContext context) {
				context.disableDefaultConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addNode( "myNode1" )
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addNode( "myNode2" )
						.addNode( "myNode3" ).inIterable().atKey( "key" )
						.addConstraintViolation();
				return false;
			}
		}
	}

	@Retention(RUNTIME)
	@Constraint(validatedBy = ClassLevelValidationAddingPropertyNodes.Validator.class)
	public @interface ClassLevelValidationAddingPropertyNodes {
		String message() default "failed";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		public static class Validator implements ConstraintValidator<ClassLevelValidationAddingPropertyNodes, Foo> {
			@Override
			public void initialize(ClassLevelValidationAddingPropertyNodes constraintAnnotation) {
			}

			@Override
			public boolean isValid(Foo value, ConstraintValidatorContext context) {
				context.disableDefaultConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addPropertyNode( "myNode1" )
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addPropertyNode( "myNode2" )
						.addPropertyNode( "myNode3" )
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addPropertyNode( "myNode4" )
						.addPropertyNode( "myNode5" )
						.inIterable()
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addPropertyNode( "myNode6" )
						.addPropertyNode( "myNode7" )
						.inIterable().atIndex( 42 )
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addPropertyNode( "myNode8" )
						.addPropertyNode( "myNode9" )
						.inIterable().atKey( "Foo" )
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addPropertyNode( "myNode10" )
						.addPropertyNode( "myNode11" )
						.inIterable()
						.addPropertyNode( "myNode12" )
						.addConstraintViolation();

				return false;
			}
		}
	}

	@Retention(RUNTIME)
	@Constraint(validatedBy = PropertyLevelValidationAddingBeanAndPropertyNodes.Validator.class)
	public @interface PropertyLevelValidationAddingBeanAndPropertyNodes {
		String message() default "failed";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		public static class Validator
				implements ConstraintValidator<PropertyLevelValidationAddingBeanAndPropertyNodes, Address> {
			@Override
			public void initialize(PropertyLevelValidationAddingBeanAndPropertyNodes constraintAnnotation) {
			}

			@Override
			public boolean isValid(Address value, ConstraintValidatorContext context) {
				context.disableDefaultConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addBeanNode()
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addPropertyNode( "myNode1" )
						.addBeanNode()
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addPropertyNode( "myNode2" )
						.inIterable()
						.addBeanNode()
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addPropertyNode( "myNode3" )
						.inIterable().atIndex( 84 )
						.addBeanNode()
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addPropertyNode( "myNode4" )
						.inIterable().atKey( "AnotherKey" )
						.addBeanNode()
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addBeanNode()
						.inIterable()
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addBeanNode()
						.inIterable().atIndex( 42 )
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addBeanNode()
						.inIterable().atKey( "Key" )
						.addConstraintViolation();

				return false;
			}
		}
	}

	@Retention(RUNTIME)
	@Constraint(validatedBy = CrossParameterValidationAddingParameterBeanAndPropertyNodes.Validator.class)
	public @interface CrossParameterValidationAddingParameterBeanAndPropertyNodes {
		String message() default "failed";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		@SupportedValidationTarget(ValidationTarget.PARAMETERS)
		public static class Validator
				implements ConstraintValidator<CrossParameterValidationAddingParameterBeanAndPropertyNodes, java.lang.Object[]> {
			@Override
			public void initialize(CrossParameterValidationAddingParameterBeanAndPropertyNodes constraintAnnotation) {
			}

			@Override
			public boolean isValid(java.lang.Object[] value, ConstraintValidatorContext context) {
				context.disableDefaultConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addParameterNode( 0 )
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addParameterNode( 0 )
						.addBeanNode()
						.addConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addParameterNode( 0 )
						.addPropertyNode( "myNode1" )
						.inIterable().atIndex( 23 )
						.addBeanNode()
						.addConstraintViolation();

				return false;
			}
		}
	}

	@Retention(RUNTIME)
	@Constraint(validatedBy = FieldLevelValidationAddingParameterNode.Validator.class)
	public @interface FieldLevelValidationAddingParameterNode {
		String message() default "failed";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		public static class Validator
				implements ConstraintValidator<FieldLevelValidationAddingParameterNode, String> {
			@Override
			public void initialize(FieldLevelValidationAddingParameterNode constraintAnnotation) {
			}

			@Override
			public boolean isValid(String value, ConstraintValidatorContext context) {
				context.disableDefaultConstraintViolation();

				context.buildConstraintViolationWithTemplate( context.getDefaultConstraintMessageTemplate() )
						.addParameterNode( 0 )
						.addConstraintViolation();

				return false;
			}
		}
	}

	private static class MyParameterNameProvider extends DefaultParameterNameProvider {

		@Override
		protected String getPrefix() {
			return "param";
		}

	}
}
