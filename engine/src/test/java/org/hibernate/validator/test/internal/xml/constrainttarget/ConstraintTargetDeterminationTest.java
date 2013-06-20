/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.xml.constrainttarget;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.pathWith;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidatingProxy;
import static org.testng.Assert.fail;

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
		catch ( ConstraintViolationException cve ) {
			assertThat( cve.getConstraintViolations() ).containsOnlyPaths(
					pathWith().method( "getNumberOfOrders" ).returnValue()
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
		catch ( ConstraintViolationException cve ) {
			assertThat( cve.getConstraintViolations() ).containsOnlyPaths(
					pathWith().method( "placeOrder" )
							.crossParameter()
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
		catch ( ConstraintViolationException cve ) {
			assertThat( cve.getConstraintViolations() ).containsOnlyPaths(
					pathWith().method( "cancelOrder" ).crossParameter(),
					pathWith().method( "cancelOrder" ).crossParameter()
			);
		}
	}
}
