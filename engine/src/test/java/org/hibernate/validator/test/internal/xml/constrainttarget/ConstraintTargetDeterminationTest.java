/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml.constrainttarget;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.fail;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test for automatic determination of constraints with generic and cross-parameter validator when
 * given via XML mapping.
 *
 * @author Gunnar Morling
 */
public class ConstraintTargetDeterminationTest {

	private OrderService orderService;

	@BeforeMethod
	public void setupValidator() {
		Validator validator = ValidatorUtil.getConfiguration()
				.addMapping( ConstraintTargetDeterminationTest.class.getResourceAsStream( "hv-769-mapping.xml" ) )
				.buildValidatorFactory()
				.getValidator();

		orderService = getValidatingProxy( new OrderServiceImpl(), validator );
	}

	@Test
	@TestForIssue(jiraKey = "HV-769")
	public void shouldDetermineConstraintTargetForReturnValueConstraint() {
		try {
			orderService.getNumberOfOrders( 42, true );
			fail( "Expected exception wasn't thrown" );
		}
		catch (ConstraintViolationException cve) {
			assertThat( cve.getConstraintViolations() ).containsOnlyViolations(
					violationOf( GenericAndCrossParameterConstraint.class )
							.withPropertyPath( pathWith()
									.method( "getNumberOfOrders" )
									.returnValue()
							)
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-769")
	public void shouldDetermineConstraintTargetForCrossParameterConstraint() {
		try {
			orderService.placeOrder( 42, "Best of Glen Closed", 3 );
			fail( "Expected exception wasn't thrown" );
		}
		catch (ConstraintViolationException cve) {
			assertThat( cve.getConstraintViolations() ).containsOnlyViolations(
					violationOf( GenericAndCrossParameterConstraint.class )
							.withPropertyPath( pathWith()
									.method( "placeOrder" )
									.crossParameter()
							)
			);
		}
	}

	@Test
	@TestForIssue(jiraKey = "HV-769")
	public void shouldDetermineConstraintTargetForComposedConstraint() {
		try {
			orderService.cancelOrder( 42, "Item damaged" );
			fail( "Expected exception wasn't thrown" );
		}
		catch (ConstraintViolationException cve) {
			assertThat( cve.getConstraintViolations() ).containsOnlyViolations(
					violationOf( GenericAndCrossParameterConstraint.class )
							.withPropertyPath( pathWith()
									.method( "cancelOrder" )
									.crossParameter()
							),
					violationOf( ComposedGenericAndCrossParameterConstraint.class )
							.withPropertyPath( pathWith()
									.method( "cancelOrder" )
									.crossParameter()
							)
			);
		}
	}
}
