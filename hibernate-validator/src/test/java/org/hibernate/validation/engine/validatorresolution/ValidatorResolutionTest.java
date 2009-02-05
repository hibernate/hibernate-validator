// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine.validatorresolution;

import java.util.Set;
import javax.validation.AmbiguousConstraintUsageException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.UnexpectedTypeForConstraintException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.hibernate.validation.eg.MultipleMinMax;
import static org.hibernate.validation.util.TestUtil.assertConstraintViolation;
import static org.hibernate.validation.util.TestUtil.assertNumberOfViolations;
import static org.hibernate.validation.util.TestUtil.getValidator;

/**
 * Tests for constraint validator resolution.
 *
 * @author Hardy Ferentschik
 */
public class ValidatorResolutionTest {

	@Test
	public void testValidatorResolutionForMinMax() {
		Validator validator = getValidator();

		MultipleMinMax minMax = new MultipleMinMax( "5", 5 );
		Set<ConstraintViolation<MultipleMinMax>> constraintViolations = validator.validate( minMax );
		assertNumberOfViolations( constraintViolations, 2 );
	}

	@Test
	public void testAmbigiousValidatorResolution() {
		Validator validator = getValidator();

		Foo foo = new Foo( new SerializableBar() );
		try {
			validator.validate( foo );
			fail();
		}
		catch ( AmbiguousConstraintUsageException e ) {
			assertTrue( e.getMessage().startsWith( "There are multiple validators" ) );
		}
	}

	@Test
	public void testUnexpectedType() {
		Validator validator = getValidator();

		Bar bar = new Bar();
		try {
			validator.validate( bar );
			fail();
		}
		catch ( UnexpectedTypeForConstraintException e ) {
			assertEquals( "No validator could be found for type: java.lang.Integer", e.getMessage() );
		}
	}

	@Test
	public void testMultipleSizeValidators() {
		Validator validator = getValidator();

		Suburb suburb = new Suburb();

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
		boundingBox[0] = new Coordinate( 0l, 0l );
		boundingBox[1] = new Coordinate( 0l, 1l );
		boundingBox[2] = new Coordinate( 1l, 0l );
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
		boundingBox[0] = new Coordinate( 0l, 0l );
		boundingBox[1] = new Coordinate( 0l, 1l );
		boundingBox[2] = new Coordinate( 1l, 0l );
		boundingBox[3] = new Coordinate( 1l, 1l );
		suburb.setBoundingBox( boundingBox );
		constraintViolations = validator.validate( suburb );
		assertNumberOfViolations( constraintViolations, 0 );
	}
}