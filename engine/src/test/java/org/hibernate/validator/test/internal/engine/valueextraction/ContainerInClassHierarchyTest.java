/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.valueextraction.Unwrapping;

import org.hibernate.validator.testutil.ConstraintViolationAssert;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.CandidateForTck;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.Test;

/**
 * @author Guillaume Smet
 */
@TestForIssue(jiraKey = "HV-1596")
@CandidateForTck
public class ContainerInClassHierarchyTest {

	@Test
	public void testContainerInHierarchy() {
		Validator validator = ValidatorUtil.getValidator();

		Set<ConstraintViolation<Bean>> constraintViolations = validator.validate( new Bean() );
		ConstraintViolationAssert.assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( Email.class ).withPropertyPath(
						pathWith().property( "container" ).containerElement( "<iterable element>", true, null, null, Container.class, null ) ),
				violationOf( NotNull.class ).withPropertyPath(
						pathWith().property( "container" ).property( "property" ) )
		);
	}

	private static class Bean {

		@Valid
		@Email(payload = Unwrapping.Unwrap.class)
		private Container container = new Container();
	}

	private static class Container extends ContainerParentClass {
	}

	private static class ContainerParentClass implements Iterable<String> {

		@NotNull
		private String property;

		@Override
		public Iterator<String> iterator() {
			return Arrays.asList( "valid-email@example.com", "invalid-email" ).iterator();
		}
	}
}
