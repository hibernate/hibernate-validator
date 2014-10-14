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
package org.hibernate.validator.test.internal.metadata.core;

import java.lang.reflect.Method;
import javax.validation.constraints.NotNull;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.testutil.TestForIssue;

import static java.lang.annotation.ElementType.METHOD;
import static org.testng.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
public class MetaConstraintTest {
	private ConstraintHelper constraintHelper;
	private Method barMethod;
	private NotNull constraintAnnotation;

	@BeforeClass
	public void setUp() throws Exception {
		constraintHelper = new ConstraintHelper();
		barMethod = Foo.class.getMethod( "getBar" );
		constraintAnnotation = barMethod.getAnnotation( NotNull.class );
	}

	@Test
	@TestForIssue(jiraKey = "HV-930")
	public void two_meta_constraints_for_the_same_constraint_should_be_equal() throws Exception {
		ConstraintDescriptorImpl<NotNull> constraintDescriptor1 = new ConstraintDescriptorImpl<NotNull>(
				constraintHelper, barMethod, constraintAnnotation, METHOD
		);
		ConstraintLocation location1 = ConstraintLocation.forClass( Foo.class );
		MetaConstraint<NotNull> metaConstraint1 = new MetaConstraint<NotNull>( constraintDescriptor1, location1 );


		ConstraintDescriptorImpl<NotNull> constraintDescriptor2 = new ConstraintDescriptorImpl<NotNull>(
				constraintHelper, barMethod, constraintAnnotation, METHOD
		);
		ConstraintLocation location2 = ConstraintLocation.forClass( Foo.class );
		MetaConstraint<NotNull> metaConstraint2 = new MetaConstraint<NotNull>( constraintDescriptor2, location2 );

		assertEquals(
				metaConstraint1, metaConstraint2, "Two MetaConstraint instances for the same constraint should be equal"
		);
	}

	public static class Foo {
		@NotNull
		public String getBar() {
			return null;
		}
	}
}


