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
package org.hibernate.validator.test.internal.engine.cascaded;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.testng.annotations.Test;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectPropertyPaths;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;

/**
 * Test for cascaded validation of custom iterable and map types. For those, property-level and class-level constraints
 * on the validated types themselves must be validated, but also constraints on the contained elements.
 *
 * @author Khalid Alqinyah
 * @author Gunnar Morling
 */
public class CascadedIterableMapPropertiesTest {

	@Test
	@TestForIssue(jiraKey = "HV-902")
	public void testValidateCustomIterableType() {
		Validator validator = getValidator();
		Set<ConstraintViolation<IterableExtHolder>> constraintViolations = validator.validate( new IterableExtHolder() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"iterableExt.value",
				"iterableExt[].number"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-902")
	public void testValidateCustomListType() {
		Validator validator = getValidator();
		Set<ConstraintViolation<ListExtHolder>> constraintViolations = validator.validate( new ListExtHolder() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"listExt.value",
				"listExt[1].number"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-902")
	public void testValidateCustomMapType() {
		Validator validator = getValidator();
		Set<ConstraintViolation<MapExtHolder>> constraintViolations = validator.validate( new MapExtHolder() );
		assertCorrectPropertyPaths(
				constraintViolations,
				"mapExt.value",
				"mapExt[second].number"
		);
	}

	class IterableExtHolder {

		@Valid
		IterableExt iterableExt = new IterableExt();
	}

	class IterableExt implements Iterable<IntWrapper> {

		@NotNull
		Integer value = null;

		@Override
		public Iterator<IntWrapper> iterator() {
			return Arrays.asList( new IntWrapper( 2 ), new IntWrapper( 1 ), new IntWrapper( 5 ) ).iterator();
		}
	}

	class ListExtHolder {

		@Valid
		ListExt listExt = new ListExt();
	}

	@SuppressWarnings("serial")
	class ListExt extends ArrayList<IntWrapper> {

		@NotNull
		Integer value = null;

		public ListExt() {
			super( Arrays.asList( new IntWrapper( 2 ), new IntWrapper( 1 ), new IntWrapper( 5 ) ) );
		}
	}

	class MapExtHolder {

		@Valid
		MapExt mapExt = new MapExt();
	}

	@SuppressWarnings("serial")
	class MapExt extends HashMap<String, IntWrapper> {

		@NotNull
		Integer value = null;

		public MapExt() {
			this.put( "first", new IntWrapper( 2 ) );
			this.put( "second", new IntWrapper( 1 ) );
			this.put( "third", new IntWrapper( 4 ) );
		}
	}

	class IntWrapper {

		@Min(value = 2)
		Integer number;

		public IntWrapper(Integer number) {
			this.number = number;
		}
	}
}
