/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.valueextraction.Unwrapping;
import javax.validation.valueextraction.ValueExtractor;

import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.testutils.CandidateForTck;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for JavaFX {@link ValueExtractor}s.
 *
 * @author Khalid Alqinyah
 * @author Hardy Ferentschik
 * @author Guillaume Smet
 */
@SuppressWarnings("restriction")
@CandidateForTck
public class JavaFXValueExtractorsTest {

	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void testJavaFXPropertyDefaultUnwrapping() {
		Set<ConstraintViolation<BasicPropertiesEntity>> constraintViolations = validator.validate( new BasicPropertiesEntity() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Max.class ).withProperty( "doubleProperty" ),
				violationOf( Min.class ).withProperty( "integerProperty" ),
				violationOf( AssertTrue.class ).withProperty( "booleanProperty" )
		);
	}

	@Test
	public void testValueExtractionForPropertyList() {
		Set<ConstraintViolation<ListPropertyEntity>> constraintViolations = validator.validate( ListPropertyEntity.valid() );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( ListPropertyEntity.invalidList() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "listProperty" )
		);

		constraintViolations = validator.validate( ListPropertyEntity.invalidListElement() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "listProperty" )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 0, ListProperty.class, 0 )
						)
		);
	}

	@Test
	public void testValueExtractionForPropertySet() {
		Set<ConstraintViolation<SetPropertyEntity>> constraintViolations = validator.validate( SetPropertyEntity.valid() );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( SetPropertyEntity.invalidSet() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "setProperty" )
		);

		constraintViolations = validator.validate( SetPropertyEntity.invalidSetElement() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "setProperty" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, null, SetProperty.class, 0 )
						)
		);

	}

	@Test
	public void testValueExtractionForPropertyMap() {
		Set<ConstraintViolation<MapPropertyEntity>> constraintViolations = validator.validate( MapPropertyEntity.valid() );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( MapPropertyEntity.invalidMap() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class ).withProperty( "mapProperty" )
		);

		constraintViolations = validator.validate( MapPropertyEntity.invalidMapKey() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withPropertyPath( pathWith()
								.property( "mapProperty" )
								.containerElement( NodeImpl.MAP_KEY_NODE_NAME, true, "app", null, MapProperty.class, 0 )
						)
		);

		constraintViolations = validator.validate( MapPropertyEntity.invalidMapValue() );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Email.class )
						.withPropertyPath( pathWith()
								.property( "mapProperty" )
								.containerElement( NodeImpl.MAP_VALUE_NODE_NAME, true, "pear", null, MapProperty.class, 1 )
						)
		);
	}

	@Test
	public void testJavaFXPropertySkipUnwrapping() {
		Set<ConstraintViolation<DefaultUnwrappingEntity>> constraintViolationsDefault = validator.validate( new DefaultUnwrappingEntity() );
		assertThat( constraintViolationsDefault ).containsOnlyViolations(
				violationOf( NotNull.class ).withProperty( "property" )
		);

		Set<ConstraintViolation<SkipUnwrappingEntity>> constraintViolationsSkip = validator.validate( new SkipUnwrappingEntity() );
		assertNoViolations( constraintViolationsSkip );
	}

	public class BasicPropertiesEntity {

		@Max(value = 3)
		private ReadOnlyDoubleWrapper doubleProperty = new ReadOnlyDoubleWrapper( 4.5 );

		@Min(value = 3)
		private IntegerProperty integerProperty = new SimpleIntegerProperty( 2 );

		@AssertTrue
		private ReadOnlyBooleanProperty booleanProperty = new SimpleBooleanProperty( false );
	}

	public static class ListPropertyEntity {

		@Size(min = 3)
		private ListProperty<@Size(min = 4) String> listProperty;

		private ListPropertyEntity(ObservableList<String> innerList) {
			this.listProperty = new ReadOnlyListWrapper<String>( innerList );
		}

		public static ListPropertyEntity valid() {
			return new ListPropertyEntity( FXCollections.observableArrayList( "apple", "pear", "cherry" ) );
		}

		public static ListPropertyEntity invalidList() {
			return new ListPropertyEntity( FXCollections.observableArrayList( "apple" ) );
		}

		public static ListPropertyEntity invalidListElement() {
			return new ListPropertyEntity( FXCollections.observableArrayList( "app", "pear", "cherry" ) );
		}
	}

	public static class SetPropertyEntity {

		@Size(min = 3)
		private SetProperty<@Size(min = 4) String> setProperty;

		private SetPropertyEntity(ObservableSet<String> innerList) {
			this.setProperty = new ReadOnlySetWrapper<String>( innerList );
		}

		public static SetPropertyEntity valid() {
			return new SetPropertyEntity( FXCollections.observableSet( "apple", "pear", "cherry" ) );
		}

		public static SetPropertyEntity invalidSet() {
			return new SetPropertyEntity( FXCollections.observableSet( "apple" ) );
		}

		public static SetPropertyEntity invalidSetElement() {
			return new SetPropertyEntity( FXCollections.observableSet( "app", "pear", "cherry" ) );
		}
	}

	public static class MapPropertyEntity {

		@Size(min = 3)
		private MapProperty<@Size(min = 4) String, @Email String> mapProperty = new ReadOnlyMapWrapper<String, String>();

		private MapPropertyEntity(ObservableMap<String, String> innerMap) {
			this.mapProperty = new ReadOnlyMapWrapper<String, String>( innerMap );
		}

		public static MapPropertyEntity valid() {
			ObservableMap<String, String> innerMap = FXCollections.observableHashMap();
			innerMap.put( "apple", "apple@example.com" );
			innerMap.put( "pear", "pear@example.com" );
			innerMap.put( "cherry", "cherry@example.com" );

			return new MapPropertyEntity( innerMap );
		}

		public static MapPropertyEntity invalidMap() {
			return new MapPropertyEntity( FXCollections.observableHashMap() );
		}

		public static MapPropertyEntity invalidMapKey() {
			ObservableMap<String, String> innerMap = FXCollections.observableHashMap();
			innerMap.put( "app", "apple@example.com" );
			innerMap.put( "pear", "pear@example.com" );
			innerMap.put( "cherry", "cherry@example.com" );

			return new MapPropertyEntity( innerMap );
		}

		public static MapPropertyEntity invalidMapValue() {
			ObservableMap<String, String> innerMap = FXCollections.observableHashMap();
			innerMap.put( "apple", "apple@example.com" );
			innerMap.put( "pear", "pear" );
			innerMap.put( "cherry", "cherry@example.com" );

			return new MapPropertyEntity( innerMap );
		}
	}

	public class DefaultUnwrappingEntity {

		@NotNull
		private StringProperty property = new SimpleStringProperty( null );
	}

	public class SkipUnwrappingEntity {

		@NotNull(payload = { Unwrapping.Skip.class })
		private StringProperty property = new SimpleStringProperty( null );
	}
}
