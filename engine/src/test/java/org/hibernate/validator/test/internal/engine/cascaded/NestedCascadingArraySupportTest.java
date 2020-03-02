/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.Test;

@TestForIssue(jiraKey = "HV-1237")
public class NestedCascadingArraySupportTest {

	// HV-1428 Container element support is disabled for arrays
	@Test(enabled = false)
	public void testNestedOnArray() {
		Validator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.addValueExtractor( new ReferenceValueExtractor() )
				.buildValidatorFactory()
				.getValidator();

		Set<ConstraintViolation<CinemaArray>> constraintViolations = validator.validate( CinemaArray.validCinemaArray() );

		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( CinemaArray.invalidCinemaArray() );

		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "array" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, Object[].class, null )
								.containerElement( NodeImpl.LIST_ELEMENT_NODE_NAME, true, null, 0, List.class, 0 )
						),
				violationOf( NotNull.class )
						.withPropertyPath( pathWith()
								.property( "array" )
								.containerElement( NodeImpl.ITERABLE_ELEMENT_NODE_NAME, true, null, 0, Object[].class, null )
								.property( "visitor", true, null, 1, List.class, 0 )
								.property( "name", Reference.class, 0 )
						)
		);
	}

	@SuppressWarnings({ "unused", "unchecked" })
	private static class CinemaArray {

		private List<@NotNull @Valid Cinema>[] array;

		private static CinemaArray validCinemaArray() {
			CinemaArray cinemaArray = new CinemaArray();

			cinemaArray.array = new List[]{ Arrays.asList( new Cinema( "cinema1", new SomeReference<>( new Visitor( "Name 1" ) ) ) ) };

			return cinemaArray;
		}

		private static CinemaArray invalidCinemaArray() {
			CinemaArray cinemaArray = new CinemaArray();

			cinemaArray.array = new List[]{ Arrays.asList( null, new Cinema( "cinema2", new SomeReference<>( new Visitor() ) ) ) };

			return cinemaArray;
		}
	}
}
