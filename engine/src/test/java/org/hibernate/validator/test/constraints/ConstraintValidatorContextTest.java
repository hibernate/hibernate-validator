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
import java.util.Set;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Payload;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

/**
 * Tests for the {@link javax.validation.ConstraintValidatorContext} API.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorContextTest {

	private Validator validator;

	@BeforeMethod
	public void setUp() {
		validator = ValidatorUtil.getValidator();
	}

	/**
	 * HV-198
	 */
	@Test
	public void testCorrectSubNodePath() {
		Item item = new Item();
		item.interval = new Interval();
		item.interval.start = 10;
		item.interval.end = 5;

		Set<ConstraintViolation<Item>> constraintViolations = validator.validate( item );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "interval.start" );
	}

	/**
	 * HV-208
	 */
	@Test
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
	public void testValidation() {
		Set<ConstraintViolation<MyObject>> constraintViolations = validator.validate( new MyObject() );

		assertNumberOfViolations( constraintViolations, 4 );
		assertCorrectPropertyPaths( constraintViolations, "field1", "field2", "myNode1", "myNode2[key].myNode3" );
	}

	@MyClassLevelValidation
	public static class MyObject {
		@NotNull
		String field1;

		@NotNull
		String field2;
	}

	@Retention(RUNTIME)
	@Constraint(validatedBy = MyClassLevelValidation.Validator.class)
	public @interface MyClassLevelValidation {
		String message() default "failed";

		Class<?>[] groups() default { };

		Class<? extends Payload>[] payload() default { };

		public static class Validator implements ConstraintValidator<MyClassLevelValidation, MyObject> {
			public void initialize(MyClassLevelValidation constraintAnnotation) {
			}

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
}
