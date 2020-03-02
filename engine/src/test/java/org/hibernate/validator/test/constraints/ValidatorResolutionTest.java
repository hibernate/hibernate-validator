/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraints;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class ValidatorResolutionTest {

	@Test
	public void testResolutionOfMultipleSizeValidators() {
		Validator validator = ValidatorUtil.getValidator();

		Suburb suburb = new Suburb();

		List<Integer> postcodes = new ArrayList<Integer>();
		postcodes.add( 12345 );
		suburb.setIncludedPostCodes( postcodes );

		// all values are null and should pass
		Set<ConstraintViolation<Suburb>> constraintViolations = validator.validate( suburb );
		assertNoViolations( constraintViolations );

		suburb.setName( "" );
		constraintViolations = validator.validate( suburb );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withMessage( "size must be between 5 and 10" )
						.withProperty( "name" )
						.withRootBeanClass( Suburb.class )
						.withInvalidValue( "" )
		);

		suburb.setName( "Hoegsbo" );
		constraintViolations = validator.validate( suburb );
		assertNoViolations( constraintViolations );

		suburb.addFacility( Suburb.Facility.SHOPPING_MALL, false );
		constraintViolations = validator.validate( suburb );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withMessage( "size must be between 2 and 2" )
						.withProperty( "facilities" )
						.withRootBeanClass( Suburb.class )
						.withInvalidValue( suburb.getFacilities() )
		);

		suburb.addFacility( Suburb.Facility.BUS_TERMINAL, true );
		constraintViolations = validator.validate( suburb );
		assertNoViolations( constraintViolations );

		suburb.addStreetName( "Sikelsgatan" );
		constraintViolations = validator.validate( suburb );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withMessage( "size must be between 2 and 2147483647" )
						.withProperty( "streetNames" )
						.withRootBeanClass( Suburb.class )
						.withInvalidValue( suburb.getStreetNames() )
		);

		suburb.addStreetName( "Marklandsgatan" );
		constraintViolations = validator.validate( suburb );
		assertNoViolations( constraintViolations );

		Coordinate[] boundingBox = new Coordinate[3];
		boundingBox[0] = new Coordinate( 0L, 0L );
		boundingBox[1] = new Coordinate( 0L, 1L );
		boundingBox[2] = new Coordinate( 1L, 0L );
		suburb.setBoundingBox( boundingBox );
		constraintViolations = validator.validate( suburb );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Size.class )
						.withMessage( "size must be between 4 and 1000" )
						.withProperty( "boundingBox" )
						.withRootBeanClass( Suburb.class )
						.withInvalidValue( suburb.getBoundingBox() )
		);

		boundingBox = new Coordinate[4];
		boundingBox[0] = new Coordinate( 0L, 0L );
		boundingBox[1] = new Coordinate( 0L, 1L );
		boundingBox[2] = new Coordinate( 1L, 0L );
		boundingBox[3] = new Coordinate( 1L, 1L );
		suburb.setBoundingBox( boundingBox );
		constraintViolations = validator.validate( suburb );
		assertNoViolations( constraintViolations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-233")
	public void testObjectArraysAndPrimitiveArraysAreSubtypesOfObject() {
		Validator validator = ValidatorUtil.getValidator();

		Foo testEntity = new Foo( new org.hibernate.validator.test.constraints.Object[] { }, new int[] { } );
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( testEntity );
		assertNoViolations( constraintViolations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-233")
	public void testObjectArraysAndPrimitiveArraysAreSubtypesOfClonable() {
		Validator validator = ValidatorUtil.getValidator();

		Bar testEntity = new Bar( new org.hibernate.validator.test.constraints.Object[] { }, new int[] { } );
		Set<ConstraintViolation<Bar>> constraintViolations = validator.validate( testEntity );
		assertNoViolations( constraintViolations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-233")
	public void testObjectArraysAndPrimitiveArraysAreSubtypesOfSerializable() {
		Validator validator = ValidatorUtil.getValidator();

		Fubar testEntity = new Fubar( new org.hibernate.validator.test.constraints.Object[] { }, new int[] { } );
		Set<ConstraintViolation<Fubar>> constraintViolations = validator.validate( testEntity );
		assertNoViolations( constraintViolations );
	}

	@Test
	@TestForIssue(jiraKey = "HV-233")
	public void testSubTypeArrayIsSubtypeOfSuperTypeArray() {
		Validator validator = ValidatorUtil.getValidator();

		SubTypeEntity testEntity = new SubTypeEntity( new SubType[] { } );
		Set<ConstraintViolation<SubTypeEntity>> constraintViolations = validator.validate( testEntity );
		assertNoViolations( constraintViolations );
	}

	@SuppressWarnings("unused")
	public class Foo {
		@org.hibernate.validator.test.constraints.Object
		private final org.hibernate.validator.test.constraints.Object[] objectArray;

		@org.hibernate.validator.test.constraints.Object
		private final int[] intArray;

		public Foo(org.hibernate.validator.test.constraints.Object[] objectArray, int[] intArray) {
			this.objectArray = objectArray;
			this.intArray = intArray;
		}
	}

	@SuppressWarnings("unused")
	public class Bar {
		@org.hibernate.validator.test.constraints.Cloneable
		private final org.hibernate.validator.test.constraints.Object[] objectArray;

		@org.hibernate.validator.test.constraints.Cloneable
		private final int[] intArray;

		public Bar(org.hibernate.validator.test.constraints.Object[] objectArray, int[] intArray) {
			this.objectArray = objectArray;
			this.intArray = intArray;
		}
	}

	@SuppressWarnings("unused")
	public class Fubar {
		@Serializable
		private final org.hibernate.validator.test.constraints.Object[] objectArray;

		@Serializable
		private final int[] intArray;

		public Fubar(org.hibernate.validator.test.constraints.Object[] objectArray, int[] intArray) {
			this.objectArray = objectArray;
			this.intArray = intArray;
		}
	}

	@SuppressWarnings("unused")
	public class SubTypeEntity {
		@SuperTypeArray
		private final SubType[] subTypeArray;

		public SubTypeEntity(SubType[] subTypeArray) {
			this.subTypeArray = subTypeArray;
		}
	}
}
