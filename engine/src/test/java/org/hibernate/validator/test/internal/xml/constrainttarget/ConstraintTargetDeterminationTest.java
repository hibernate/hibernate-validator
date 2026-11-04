/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml.constrainttarget;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidatingProxy;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for automatic determination of constraints with generic and cross-parameter validator when
 * given via XML mapping.
 *
 * @author Gunnar Morling
 */
public class ConstraintTargetDeterminationTest {

	private OrderService orderService;

	@BeforeEach
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
		assertThatThrownBy( () -> orderService.getNumberOfOrders( 42, true ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> {
					ConstraintViolationException cve = (ConstraintViolationException) e;
					assertThat( cve.getConstraintViolations() ).containsOnlyViolations(
							violationOf( GenericAndCrossParameterConstraint.class )
									.withPropertyPath( pathWith()
											.method( "getNumberOfOrders" )
											.returnValue()
									)
					);
				} );
	}

	@Test
	@TestForIssue(jiraKey = "HV-769")
	public void shouldDetermineConstraintTargetForCrossParameterConstraint() {
		assertThatThrownBy( () -> orderService.placeOrder( 42, "Best of Glen Closed", 3 ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> {
					ConstraintViolationException cve = (ConstraintViolationException) e;
					assertThat( cve.getConstraintViolations() ).containsOnlyViolations(
							violationOf( GenericAndCrossParameterConstraint.class )
									.withPropertyPath( pathWith()
											.method( "placeOrder" )
											.crossParameter()
									)
					);
				} );
	}

	@Test
	@TestForIssue(jiraKey = "HV-769")
	public void shouldDetermineConstraintTargetForComposedConstraint() {
		assertThatThrownBy( () -> orderService.cancelOrder( 42, "Item damaged" ) )
				.isInstanceOf( ConstraintViolationException.class )
				.satisfies( e -> {
					ConstraintViolationException cve = (ConstraintViolationException) e;
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
				} );
	}
}
