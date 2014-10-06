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

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.UnexpectedTypeException;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * Tests for {@link org.hibernate.validator.internal.engine.valuehandling.JavaFXPropertyValueUnwrapper}.
 *
 * @author Khalid Alqinyah
 * @author Hardy Ferentschik
 */
public class JavaFXPropertyValueUnwrapperTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void testJavaFXPropertyDefaultUnwrapping() {
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"doubleProperty",
				"integerProperty",
				"booleanProperty"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				Max.class,
				Min.class,
				AssertTrue.class
		);
	}

	@Test
	public void testJavaFXPropertyExplicitUnwrapping() {
		Set<ConstraintViolation<Fubar>> constraintViolations = validator.validate( new Fubar() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"listProperty"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				Size.class
		);
	}

	@Test
	public void testValidatorForWrapperAndWrappedValueThrowsException() {
		try {
			validator.validate( new Bar1() );
			fail( "Should have thrown an exception" );
		}
		catch ( UnexpectedTypeException e ) {
			assertTrue( e.getMessage().startsWith( "HV000186" ) );
		}
	}

	@Test
	public void testJavaFXPropertyExplicitUnwrappingNotNull() {
		Set<ConstraintViolation<Bar2>> constraintViolations = validator.validate( new Bar2() );
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"property"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				NotNull.class
		);
	}

	@Test
	public void testJavaFXPropertySkipUnwrapping() {
		assertNumberOfViolations( validator.validate( new Bar3() ), 0 );
	}

	@SuppressWarnings("unused")
	public class Foo {
		@UnwrapValidatedValue
		@Max(value = 3)
		ReadOnlyDoubleWrapper doubleProperty = new ReadOnlyDoubleWrapper( 4.5 );

		@UnwrapValidatedValue
		@Min(value = 3)
		IntegerProperty integerProperty = new SimpleIntegerProperty( 2 );

		@UnwrapValidatedValue
		@AssertTrue
		ReadOnlyBooleanProperty booleanProperty = new SimpleBooleanProperty( false );
	}

	@SuppressWarnings("unused")
	public class Fubar {
		// Need to explicitly unwrap, since ReadOnlyListProperty in itself implements List
		@UnwrapValidatedValue
		@Size(min = 5)
		ReadOnlyListProperty listProperty = new ReadOnlyListWrapper( FXCollections.observableArrayList( 1, 2, 3 ) );
	}

	@SuppressWarnings("unused")
	public class Bar1 {
		@NotNull
		MapProperty property = new SimpleMapProperty( null );
	}

	@SuppressWarnings("unused")
	public class Bar2 {
		@UnwrapValidatedValue(true)
		@NotNull
		MapProperty property = new SimpleMapProperty( null );
	}

	@SuppressWarnings("unused")
	public class Bar3 {
		@UnwrapValidatedValue(false)
		@NotNull
		MapProperty property = new SimpleMapProperty( null );
	}

}
