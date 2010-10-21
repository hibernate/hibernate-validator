/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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

package org.hibernate.validator.test.constraints.boolcomposition;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.test.util.TestUtil;

import static org.hibernate.validator.test.util.TestUtil.assertCorrectConstraintTypes;
import static org.hibernate.validator.test.util.TestUtil.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.test.util.TestUtil.assertCorrectPropertyPaths;
import static org.hibernate.validator.test.util.TestUtil.assertNumberOfViolations;

/**
 * @author Federico Mancini
 * @author Dag Hovland
 */
public class BoolCompositeConstraintTest {

	/**
	 * HV-390
	 */
	@Test
	public void testCorrectAnnotationTypeWithBoolOr() {
		Validator currentValidator = TestUtil.getValidator();

		Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
				new Person( "K", "G" )
		);

		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectConstraintTypes( constraintViolations, PatternOrSize.class, NotNullAndSize.class );
		assertCorrectPropertyPaths(constraintViolations, "name", "name");

		constraintViolations = currentValidator.validate(
				new Person(
						"G", "Gerhard"
				)
		);
		assertNumberOfViolations( constraintViolations, 1 );
		assertCorrectConstraintTypes( constraintViolations, PatternOrSize.class );
		assertCorrectPropertyPaths(constraintViolations, "nickName");
	}

	/**
	 * HV-390
	 */
	@Test
	public void testCorrectAnnotationTypeWithBoolAnd() {
		Validator currentValidator = TestUtil.getValidator();

		Set<ConstraintViolation<Person>> constraintViolations = currentValidator.validate(
				new Person(
						"G", "K"
				)
		);

		assertNumberOfViolations( constraintViolations, 2 );
		assertCorrectConstraintTypes( constraintViolations, PatternOrSize.class, NotNullAndSize.class );
		assertCorrectPropertyPaths(constraintViolations, "name", "nickName");

		constraintViolations = currentValidator.validate(
				new Person(
						"L", "G"
				)
		);
		assertNumberOfViolations( constraintViolations, 3 );
		assertCorrectConstraintTypes(
				constraintViolations, NotNullAndSize.class, PatternOrSize.class, PatternOrSize.class
		);
		assertCorrectPropertyPaths(constraintViolations, "name", "name", "nickName");
	}
}

