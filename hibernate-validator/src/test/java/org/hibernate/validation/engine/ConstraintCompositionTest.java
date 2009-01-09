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
package org.hibernate.validation.engine;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import static org.junit.Assert.fail;
import org.junit.Test;

import static org.hibernate.validation.util.TestUtil.getHibernateValidator;
import static org.hibernate.validation.util.TestUtil.assertNumberOfViolations;
import static org.hibernate.validation.util.TestUtil.assertConstraintViolation;

import org.hibernate.validation.constraints.NotNullConstraint;
import org.hibernate.validation.constraints.SizeContraint;
import org.hibernate.validation.constraints.PatternConstraint;
import org.hibernate.validation.eg.FrenchAddress;

/**
 * Tests for the implementation of <code>Validator</code>.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintCompositionTest {

	@Test
	public void testComposition() {
		Validator validator = getHibernateValidator();

		FrenchAddress address = new FrenchAddress();
		address.setAddressline1( "10 rue des Treuils" );
		address.setAddressline2( "BP 12 " );
		address.setCity( "Bordeaux" );
		Set<ConstraintViolation<FrenchAddress>> constraintViolations = validator.validate( address );
		assertNumberOfViolations( constraintViolations, 1 );
		assertConstraintViolation(
				constraintViolations.iterator().next(),
				"may not be null",
				NotNullConstraint.class,
				FrenchAddress.class,
				null,
				"zipCode"
		);


		address.setZipCode( "abc" );
		constraintViolations = validator.validate( address );
		assertNumberOfViolations( constraintViolations, 3 );
		for ( ConstraintViolation violation : constraintViolations ) {
			if ( violation.getInterpolatedMessage().equals( "A french zip code has a length of 5" ) ) {
				assertConstraintViolation(
						violation,
						"A french zip code has a length of 5",
						SizeContraint.class,
						FrenchAddress.class,
						"abc",
						"zipCode"
				);
			}
			else if ( violation.getInterpolatedMessage().equals( "must match \"d*\"" ) ) {
				assertConstraintViolation(
						violation,
						"must match \"d*\"",
						PatternConstraint.class,
						FrenchAddress.class,
						"abc",
						"zipCode"
				);
			}
			else if ( violation.getInterpolatedMessage().equals( "must match \".....\"" ) ) {
				assertConstraintViolation(
						violation,
						"must match \".....\"" ,
						PatternConstraint.class,
						FrenchAddress.class,
						"abc",
						"zipCode"
				);
			}
			else {
				fail( "Wrong violation found." );
			}
		}


		address.setZipCode( "123" );
		constraintViolations = validator.validate( address );
		assertNumberOfViolations( constraintViolations, 2 );
		for ( ConstraintViolation violation : constraintViolations ) {
			if ( violation.getInterpolatedMessage().equals( "A french zip code has a length of 5" ) ) {
				assertConstraintViolation(
						violation,
						"A french zip code has a length of 5",
						SizeContraint.class,
						FrenchAddress.class,
						"123",
						"zipCode"
				);
			}
			else if ( violation.getInterpolatedMessage().equals( "must match \".....\"" ) ) {
				assertConstraintViolation(
						violation,
						"must match \".....\"" ,
						PatternConstraint.class,
						FrenchAddress.class,
						"123",
						"zipCode"
				);
			}
			else {
				fail( "Wrong violation found." );
			}
		}

		address.setZipCode( "33023" );
		constraintViolations = validator.validate( address );
		assertNumberOfViolations( constraintViolations, 0 );
	}
}