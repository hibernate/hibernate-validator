/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv.password;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.hibernate.validator.spi.password.PasswordStrengthResult;
import org.hibernate.validator.spi.password.PasswordStrengthScore;

import org.testng.annotations.Test;

public class PasswordStrengthAdapterTest {

	@Test
	public void nbvcxzWeakPassword() {
		NbvcxzPasswordStrengthEstimator estimator = new NbvcxzPasswordStrengthEstimator();
		PasswordStrengthResult result = estimator.estimate( "password".toCharArray() );
		assertNotNull( result );
		assertTrue( result.score() < PasswordStrengthScore.STRONG );
	}

	@Test
	public void nbvcxzStrongPassword() {
		NbvcxzPasswordStrengthEstimator estimator = new NbvcxzPasswordStrengthEstimator();
		PasswordStrengthResult result = estimator.estimate( "c0rr3ct-h0rs3-b4tt3ry-st4pl3!".toCharArray() );
		assertNotNull( result );
		assertTrue( result.score() >= PasswordStrengthScore.STRONG );
	}

	@Test
	public void zxcvbn4jWeakPassword() {
		Zxcvbn4jPasswordStrengthEstimator estimator = new Zxcvbn4jPasswordStrengthEstimator();
		PasswordStrengthResult result = estimator.estimate( "password".toCharArray() );
		assertNotNull( result );
		assertTrue( result.score() < PasswordStrengthScore.STRONG );
	}

	@Test
	public void zxcvbn4jStrongPassword() {
		Zxcvbn4jPasswordStrengthEstimator estimator = new Zxcvbn4jPasswordStrengthEstimator();
		PasswordStrengthResult result = estimator.estimate( "c0rr3ct-h0rs3-b4tt3ry-st4pl3!".toCharArray() );
		assertNotNull( result );
		assertTrue( result.score() >= PasswordStrengthScore.STRONG );
	}
}
