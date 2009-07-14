// $Id:$
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
package org.hibernate.validation.constraints.composition;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

import org.hibernate.validation.util.TestUtil;
import static org.hibernate.validation.util.TestUtil.assertCorrectConstraintTypes;
import static org.hibernate.validation.util.TestUtil.assertCorrectConstraintViolationMessages;
import static org.hibernate.validation.util.TestUtil.assertNumberOfViolations;

/**
 * @author Gerhard Petracek
 * @author Hardy Ferentschik
 */
public class CompositeConstraintTest {

	/**
	 * HV-182
	 */
	@Test
	public void testCorrectAnnotationTypeForWithReportAsSingleViolation() {

		Validator currentValidator = TestUtil.getValidator();

		for ( int i = 0; i < 100; i++ ) {
			Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
					new Person(
							null, "Gerhard"
					)
			);

			assertNumberOfViolations( constraintViolations, 1 );
			assertCorrectConstraintTypes( constraintViolations, ValidNameSingleViolation.class );
			assertCorrectConstraintViolationMessages( constraintViolations, "invalid name" );

			constraintViolations = currentValidator.validate(
					new Person(
							"G", "Gerhard"
					)
			);
			assertNumberOfViolations( constraintViolations, 1 );
			assertCorrectConstraintTypes( constraintViolations, ValidNameSingleViolation.class );
			assertCorrectConstraintViolationMessages( constraintViolations, "invalid name" );
		}
	}

	/**
	 * HV-182
	 */
	@Test
	public void testCorrectAnnotationTypeReportMultipleViolations() {

		Validator currentValidator = TestUtil.getValidator();

		for ( int i = 0; i < 100; i++ ) {
			Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
					new Person(
							"Gerd", null
					)
			);

			assertNumberOfViolations( constraintViolations, 1 );
			assertCorrectConstraintTypes( constraintViolations, NotNull.class );
			assertCorrectConstraintViolationMessages( constraintViolations, "may not be null" );

			constraintViolations = currentValidator.validate(
					new Person(
							"Gerd", "G"
					)
			);
			assertNumberOfViolations( constraintViolations, 1 );
			assertCorrectConstraintTypes( constraintViolations, Size.class );
			assertCorrectConstraintViolationMessages( constraintViolations, "size must be between 2 and 10" );
		}
	}
}

