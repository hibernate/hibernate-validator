/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.annotations.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;

import java.util.Set;

import jakarta.validation.ConstraintViolation;

import org.hibernate.validator.constraints.IpAddress;
import org.hibernate.validator.test.constraints.annotations.AbstractConstrainedTest;
import org.hibernate.validator.testutil.TestForIssue;

import org.testng.annotations.Test;


/**
 * Test to make sure that elements annotated with {@link IpAddress} are validated.
 *
 * @author Ivan Malutin
 */
@TestForIssue(jiraKey = "HV-2137")
public class IpAddressConstrainedTest extends AbstractConstrainedTest {

	@Test
	public void validIpAddress() {
		Foo foo = new Foo( "127.0.0.1" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertNoViolations( violations );
	}

	@Test
	public void invalidIpAddress() {
		Foo foo = new Foo( "256.256.256.256" );
		Set<ConstraintViolation<Foo>> violations = validator.validate( foo );
		assertThat( violations ).containsOnlyViolations(
				violationOf( IpAddress.class ).withMessage( "invalid IP address" )
		);
	}

	private static class Foo {
		@IpAddress
		private final String ipAddress;

		public Foo(String ipAddress) {
			this.ipAddress = ipAddress;
		}
	}
}
