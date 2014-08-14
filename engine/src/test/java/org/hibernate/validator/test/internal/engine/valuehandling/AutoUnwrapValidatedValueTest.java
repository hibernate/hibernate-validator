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
package org.hibernate.validator.test.internal.engine.valuehandling;

import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.validation.constraints.Min;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

/**
 * Tests the auto unwrapping of {@code Optional} and JavaFX properties.
 *
 * @author Khalid Alqinyah
 */
public class AutoUnwrapValidatedValueTest {

	@Test
	public void testOptionalAutoUnwrapping() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.autoUnwrapValidatedValue( true )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerOptional" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
	}

	@Test
	public void testJavaFXAutoUnwrapping() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.autoUnwrapValidatedValue( true )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Bar>> constraintViolations = validator.validate( new Bar() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerProperty" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
	}

	@Test
	public void testAutoUnwrappingSetWithProperty() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addProperty( HibernateValidatorConfiguration.AUTO_UNWRAP_VALIDATED_VALUE, "true" )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths( constraintViolations, "integerOptional" );
		assertCorrectConstraintTypes( constraintViolations, Min.class );
	}

	@Test( expectedExceptions = ValidationException.class,
			expectedExceptionsMessageRegExp = "HV000030: No validator could be found for type: java.util.Optional<java.lang.Integer>." )
	public void testAutoUnwrappingSetWithInvalidProperty() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addProperty( HibernateValidatorConfiguration.AUTO_UNWRAP_VALIDATED_VALUE, "invalid" )
				.buildValidatorFactory()
				.getValidator();

		validator.validate( new Foo() );
	}

	@Test( expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = "HV000186.*" )
	public void testAutoUnwrappingSetWithInconsistentConfiguration() {
		Validation.byProvider( HibernateValidator.class )
				.configure()
				.addProperty( HibernateValidatorConfiguration.AUTO_UNWRAP_VALIDATED_VALUE, "false" )
				.autoUnwrapValidatedValue( true )
				.buildValidatorFactory()
				.getValidator();
	}

	private class Foo {
		@Min(value = 5)
		Optional<Integer> integerOptional = Optional.of( 3 );
	}

	private class Bar {
		@Min(value = 3)
		IntegerProperty integerProperty = new SimpleIntegerProperty( 2 );
	}
}
