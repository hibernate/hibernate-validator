/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.engine.valuehandling.wrapper;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Null;
import javax.validation.executable.ExecutableValidator;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * Tests for {@link org.hibernate.validator.spi.valuehandling.wrapper.OptionalValueUnwrapper}.
 *
 * @author Khalid Alqinyah
 */
public class OptionalValueUnwrapperTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void testOptionalUnwrappedValueViolations() {
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );
		assertNumberOfViolations( constraintViolations, 4 );
		assertCorrectPropertyPaths( constraintViolations, "integerOptional", "stringOptional", "barOptional.number", "optionalLong" );
		assertCorrectConstraintTypes( constraintViolations, Min.class, NotBlank.class, Min.class, Max.class );
	}

	@Test
	public void testOptionalUnwrappedExecutableReturnValue() throws Exception {
		ExecutableValidator executableValidator = validator.forExecutables();
		Method method = Foo.class.getMethod( "getOptionalLong" );
		Set<ConstraintViolation<Foo>> constraintViolations = executableValidator.validateReturnValue(
				new Foo(),
				method,
				Optional.of( 9L )
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "getOptionalLong.<return value>" );
		assertCorrectConstraintTypes( constraintViolations, Max.class );
	}

	@Test
	public void testOptionalUnwrappedExecutableParameter() throws Exception {
		ExecutableValidator executableValidator = validator.forExecutables();
		Method method = Foo.class.getMethod( "setOptionalLong", Optional.class );
		Object [] values = {Optional.of( 2L )};
		Set<ConstraintViolation<Foo>> constraintViolations = executableValidator.validateParameters( new Foo(), method, values );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "setOptionalLong.arg0" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
	}

	@Test
	public void testOptionalUnwrappedCascadableExecutableReturnValue() throws Exception {
		ExecutableValidator executableValidator = validator.forExecutables();
		Method method = Foo.class.getMethod( "getBar" );
		Set<ConstraintViolation<Foo>> constraintViolations = executableValidator.validateReturnValue(
				new Foo(),
				method,
				Optional.of( new Bar() )
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "getBar.<return value>.number" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
	}

	@Test
	public void testOptionalUnwrappedCascadableExecutableParameter() throws Exception {
		ExecutableValidator executableValidator = validator.forExecutables();
		Method method = Foo.class.getMethod( "setBar", Optional.class );
		Object [] values = {Optional.of( new Bar() )};
		Set<ConstraintViolation<Foo>> constraintViolations = executableValidator.validateParameters( new Foo(), method, values );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "setBar.arg0.number" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
	}

	@SuppressWarnings("unused")
	private class Foo {
		@UnwrapValidatedValue
		@Min(value = 5)
		Optional<Integer> integerOptional = Optional.of( 3 );

		@UnwrapValidatedValue
		@NotBlank
		Optional<String> stringOptional = Optional.of( " " );

		@UnwrapValidatedValue
		@Max(value = 8)
		Optional<Double> doubleOptional = Optional.of( 7.5 );

		@UnwrapValidatedValue
		@Null
		Optional<Integer> emptyOptional = Optional.empty();

		@UnwrapValidatedValue
		@Valid
		Optional<Bar> barOptional = Optional.of( new Bar() );

		@UnwrapValidatedValue
		@Max(value = 5)
		public Optional<Long> getOptionalLong() {
			return Optional.of( 7L );
		}

		public void setOptionalLong(@UnwrapValidatedValue @Min(5) Optional<Long> optionalLongPara) {

		}

		@UnwrapValidatedValue
		@Valid
		public Optional<Bar> getBar() {
			return Optional.empty();
		}

		public void setBar(@UnwrapValidatedValue @Valid Optional<Bar> optionalBarPara) {

		}
	}

	@SuppressWarnings("unused")
	private class Bar {
		@Min(value = 5)
		int number = 3;
	}
}
