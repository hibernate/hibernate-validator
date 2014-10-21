/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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


