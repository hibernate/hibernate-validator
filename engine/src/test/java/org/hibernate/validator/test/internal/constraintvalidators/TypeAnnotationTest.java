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
package org.hibernate.validator.test.internal.constraintvalidators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertEquals;

/**
 * @author Khalid Alqinyah
 */
public class TypeAnnotationTest {
	private Validator validator;

	@BeforeClass
	public void setup() {
		validator = getValidator();
	}

	@Test
	public void test() {
		Person person = new Person();
		person.namesMap = new HashMap<String, String>();
		person.namesMap.put( "first", "John" );
		person.namesMap.put( "second", "" );
		person.namesMap.put( "third", "Doe" );

		Set<ConstraintViolation<Person>> errors = validator.validate( person );
		assertEquals( errors.size(), 4 );
		assertCorrectPropertyPaths( errors, "names[1]", "email", "stringProperty", "namesMap[second]" );
	}

	private class Person {
		@UnwrapValidatedValue
		private Optional<@NotBlank String> email = Optional.of( "" );

		@UnwrapValidatedValue
		private Optional<@UnwrapValidatedValue @NotBlank StringProperty> stringProperty = Optional.of( new SimpleStringProperty( "" ) );

		private List<@NotBlank String> names = Arrays.asList( "John", "", "Doe" );

		private Map<String, @NotBlank String> namesMap;
	}
}
