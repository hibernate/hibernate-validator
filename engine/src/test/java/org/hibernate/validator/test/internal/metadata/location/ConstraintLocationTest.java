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
package org.hibernate.validator.test.internal.metadata.location;

import java.lang.reflect.Member;

import org.testng.annotations.Test;

import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.testutil.TestForIssue;

import static org.testng.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintLocationTest {

	@Test
	@TestForIssue(jiraKey = "HV-930")
	public void two_constraint_locations_for_the_same_type_should_be_equal() {
		ConstraintLocation location1 = ConstraintLocation.forClass( Foo.class );
		ConstraintLocation location2 = ConstraintLocation.forClass( Foo.class );

		assertEquals( location1, location2, "Two constraint locations for the same type should be equal" );
	}

	@Test
	@TestForIssue(jiraKey = "HV-930")
	public void two_constraint_locations_for_the_same_member_should_be_equal() throws Exception {
		Member member = Foo.class.getMethod( "getBar" );
		ConstraintLocation location1 = ConstraintLocation.forProperty( member );
		ConstraintLocation location2 = ConstraintLocation.forProperty( member );

		assertEquals( location1, location2, "Two constraint locations for the same type should be equal" );
	}

	public static class Foo {
		public String getBar() {
			return null;
		}
	}
}


