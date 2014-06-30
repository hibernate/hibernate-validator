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
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyFloatWrapper;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * Tests for {@link org.hibernate.validator.internal.engine.valuehandling.JavaFXPropertyValueUnwrapper}.
 *
 * @author Khalid Alqinyah
 */
public class JavaFXPropertyValueUnwrapperTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void testJavaFXPropertyUnwrappedValueViolations() {
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );
		assertNumberOfViolations( constraintViolations, 9 );
		assertCorrectPropertyPaths(
				constraintViolations,
				"doubleProperty",
				"integerProperty",
				"stringProperty",
				"booleanProperty",
				"floatProperty",
				"objectProperty",
				"mapProperty",
				"listProperty",
				"setProperty"
		);
		assertCorrectConstraintTypes(
				constraintViolations,
				Min.class,
				NotBlank.class,
				AssertTrue.class,
				Max.class,
				Max.class,
				Size.class,
				Size.class,
				NotNull.class,
				NotNull.class
		);
	}

	@SuppressWarnings("unused")
	private class Foo {
		@UnwrapValidatedValue
		@Max(value = 3)
		ReadOnlyDoubleWrapper doubleProperty = new ReadOnlyDoubleWrapper( 4.5 );

		@UnwrapValidatedValue
		@Min(value = 3)
		IntegerProperty integerProperty = new SimpleIntegerProperty( 2 );

		@UnwrapValidatedValue
		@NotBlank
		SimpleStringProperty stringProperty = new SimpleStringProperty( "" );

		@UnwrapValidatedValue
		@AssertTrue
		ReadOnlyBooleanProperty booleanProperty = new SimpleBooleanProperty( false );

		@UnwrapValidatedValue
		@Max(value = 4)
		Property<Number> floatProperty = new ReadOnlyFloatWrapper( 5.5f );

		@UnwrapValidatedValue
		@NotNull
		ReadOnlyProperty<Object> objectProperty = new SimpleObjectProperty( null );

		@UnwrapValidatedValue
		@NotNull
		MapProperty mapProperty = new SimpleMapProperty( null );

		@UnwrapValidatedValue
		@Size(min = 5)
		ReadOnlyListProperty listProperty = new ReadOnlyListWrapper( FXCollections.observableArrayList( 1, 2, 3 ) );

		@UnwrapValidatedValue
		@Size(min = 5)
		SetProperty setProperty = new SimpleSetProperty( FXCollections.observableSet( 1, 2, 3 ) );

		@UnwrapValidatedValue
		@Min(value = 2)
		ObservableValue<Number> longProperty = new SimpleLongProperty( 4 );
	}
}
