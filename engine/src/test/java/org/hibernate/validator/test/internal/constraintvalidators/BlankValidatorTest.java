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
package org.hibernate.validator.test.internal.constraintvalidators;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.internal.constraintvalidators.NotBlankValidator;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class BlankValidatorTest {
	@Test
	public void testConstraintValidator() {
		NotBlankValidator constraintValidator = new NotBlankValidator();

		assertTrue( constraintValidator.isValid( "a", null ) );
		assertTrue( constraintValidator.isValid( null, null ) );
		assertFalse( constraintValidator.isValid( "", null ) );
		assertFalse( constraintValidator.isValid( " ", null ) );
		assertFalse( constraintValidator.isValid( "\t", null ) );
		assertFalse( constraintValidator.isValid( "\n", null ) );
	}

	@Test
	public void testNotBlank() {
		Validator validator = getValidator();
		Foo foo = new Foo();

		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( foo );
		assertNumberOfViolations( constraintViolations, 1 );

		foo.name = "";
		constraintViolations = validator.validate( foo );
		assertNumberOfViolations( constraintViolations, 1 );

		foo.name = " ";
		constraintViolations = validator.validate( foo );
		assertNumberOfViolations( constraintViolations, 1 );

		foo.name = "\t";
		constraintViolations = validator.validate( foo );
		assertNumberOfViolations( constraintViolations, 1 );

		foo.name = "\n";
		constraintViolations = validator.validate( foo );
		assertNumberOfViolations( constraintViolations, 1 );

		foo.name = "john doe";
		constraintViolations = validator.validate( foo );
		assertNumberOfViolations( constraintViolations, 0 );
	}

	class Foo {
		@NotBlank
		String name;
	}
}
