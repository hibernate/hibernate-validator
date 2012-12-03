/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertConstraintViolation;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;

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
		assertNumberOfViolations( constraintViolations, 0 );

		suburb.setName( "" );
		constraintViolations = validator.validate( suburb );
		assertNumberOfViolations( constraintViolations, 1 );
		assertConstraintViolation(
				constraintViolations.iterator().next(), "size must be between 5 and 10", Suburb.class, "", "name"
		);

		suburb.setName( "Hoegsbo" );
		constraintViolations = validator.validate( suburb );
		assertNumberOfViolations( constraintViolations, 0 );

		suburb.addFacility( Suburb.Facility.SHOPPING_MALL, false );
		constraintViolations = validator.validate( suburb );
		assertNumberOfViolations( constraintViolations, 1 );
		assertConstraintViolation(
				constraintViolations.iterator().next(),
				"size must be between 2 and 2",
				Suburb.class,
				suburb.getFacilities(),
				"facilities"
		);

		suburb.addFacility( Suburb.Facility.BUS_TERMINAL, true );
		constraintViolations = validator.validate( suburb );
		assertNumberOfViolations( constraintViolations, 0 );

		suburb.addStreetName( "Sikelsgatan" );
		constraintViolations = validator.validate( suburb );
		assertNumberOfViolations( constraintViolations, 1 );
		assertConstraintViolation(
				constraintViolations.iterator().next(),
				"size must be between 2 and 2147483647",
				Suburb.class,
				suburb.getStreetNames(),
				"streetNames"
		);

		suburb.addStreetName( "Marklandsgatan" );
		constraintViolations = validator.validate( suburb );
		assertNumberOfViolations( constraintViolations, 0 );

		Coordinate[] boundingBox = new Coordinate[3];
		boundingBox[0] = new Coordinate( 0L, 0L );
		boundingBox[1] = new Coordinate( 0L, 1L );
		boundingBox[2] = new Coordinate( 1L, 0L );
		suburb.setBoundingBox( boundingBox );
		constraintViolations = validator.validate( suburb );
		assertNumberOfViolations( constraintViolations, 1 );
		assertConstraintViolation(
				constraintViolations.iterator().next(),
				"size must be between 4 and 1000",
				Suburb.class,
				suburb.getBoundingBox(),
				"boundingBox"
		);

		boundingBox = new Coordinate[4];
		boundingBox[0] = new Coordinate( 0L, 0L );
		boundingBox[1] = new Coordinate( 0L, 1L );
		boundingBox[2] = new Coordinate( 1L, 0L );
		boundingBox[3] = new Coordinate( 1L, 1L );
		suburb.setBoundingBox( boundingBox );
		constraintViolations = validator.validate( suburb );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-233")
	public void testObjectArraysAndPrimitiveArraysAreSubtypesOfObject() {
		Validator validator = ValidatorUtil.getValidator();

		Foo testEntity = new Foo( new org.hibernate.validator.test.constraints.Object[] { }, new int[] { } );
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( testEntity );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-233")
	public void testObjectArraysAndPrimitiveArraysAreSubtypesOfClonable() {
		Validator validator = ValidatorUtil.getValidator();

		Bar testEntity = new Bar( new org.hibernate.validator.test.constraints.Object[] { }, new int[] { } );
		Set<ConstraintViolation<Bar>> constraintViolations = validator.validate( testEntity );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-233")
	public void testObjectArraysAndPrimitiveArraysAreSubtypesOfSerializable() {
		Validator validator = ValidatorUtil.getValidator();

		Fubar testEntity = new Fubar( new org.hibernate.validator.test.constraints.Object[] { }, new int[] { } );
		Set<ConstraintViolation<Fubar>> constraintViolations = validator.validate( testEntity );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-233")
	public void testSubTypeArrayIsSubtypeOfSuperTypeArray() {
		Validator validator = ValidatorUtil.getValidator();

		SubTypeEntity testEntity = new SubTypeEntity( new SubType[] { } );
		Set<ConstraintViolation<SubTypeEntity>> constraintViolations = validator.validate( testEntity );
		assertNumberOfViolations( constraintViolations, 0 );
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
