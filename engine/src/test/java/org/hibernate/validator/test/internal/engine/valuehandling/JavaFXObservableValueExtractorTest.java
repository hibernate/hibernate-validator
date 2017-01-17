/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintTypes;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.valueextraction.ValidateUnwrappedValue;

import org.hibernate.validator.internal.engine.cascading.ObservableValueExtractor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

/**
 * Tests for {@link ObservableValueExtractor}.
 *
 * @author Khalid Alqinyah
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
public class JavaFXObservableValueExtractorTest {

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
	public void testValidateUnwrappedValueNo() {
		Set<ConstraintViolation<Bar1>> constraintViolations = validator.validate( new Bar1() );
		assertNumberOfViolations( constraintViolations, 0 );
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
		// Need to explicitly unwrap, since ReadOnlyListProperty in itself implements List
		@Max(value = 3, validateUnwrappedValue = ValidateUnwrappedValue.YES)
		ReadOnlyDoubleWrapper doubleProperty = new ReadOnlyDoubleWrapper( 4.5 );

		// The value extractor of Property enables unwrapping by default
		@Min(value = 3)
		IntegerProperty integerProperty = new SimpleIntegerProperty( 2 );

		@AssertTrue
		ReadOnlyBooleanProperty booleanProperty = new SimpleBooleanProperty( false );
	}

	@SuppressWarnings("unused")
	public class Fubar {
		// Need to explicitly unwrap, since ReadOnlyListProperty in itself implements List
		@Size(min = 5, validateUnwrappedValue = ValidateUnwrappedValue.YES)
		ReadOnlyListProperty listProperty = new ReadOnlyListWrapper( FXCollections.observableArrayList( 1, 2, 3 ) );
	}

	@SuppressWarnings("unused")
	public class Bar1 {
		@NotNull(validateUnwrappedValue = ValidateUnwrappedValue.NO)
		MapProperty property = new SimpleMapProperty( null );
	}

	@SuppressWarnings("unused")
	public class Bar2 {
		@NotNull
		MapProperty property = new SimpleMapProperty( null );
	}

	@SuppressWarnings("unused")
	public class Bar3 {
		@NotNull(validateUnwrappedValue = ValidateUnwrappedValue.NO)
		MapProperty property = new SimpleMapProperty( null );
	}

}
