/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.testutil;


import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.*;

import java.util.Map;

import jakarta.validation.ConstraintValidatorFactory;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PreconfiguredConstraintValidatorFactoryTest {

	private ConstraintValidatorFactory delegatedConstraintValidatorFactory;

	@BeforeMethod
	public void setUp() {
		delegatedConstraintValidatorFactory = createMock( ConstraintValidatorFactory.class );
	}

	@Test
	public void testGetInstanceWithPreconfiguredValidator() {
		CountValidationCallsValidator constraintValidator = new CountValidationCallsValidator();

		PreconfiguredConstraintValidatorFactory constraintValidatorFactory = PreconfiguredConstraintValidatorFactory.builder()
				.delegated( delegatedConstraintValidatorFactory )
				.defaultValidators( Map.of( CountValidationCallsValidator.class, constraintValidator ) )
				.build();

		assertThat( constraintValidatorFactory.getInstance( CountValidationCallsValidator.class ) )
				.isEqualTo( constraintValidator );
	}

	@Test
	public void testGetInstanceWithDefaultValidator() {
		CountValidationCallsValidator constraintValidator = new CountValidationCallsValidator();

		expect( delegatedConstraintValidatorFactory.getInstance( CountValidationCallsValidator.class ) ).andReturn( constraintValidator );

		PreconfiguredConstraintValidatorFactory constraintValidatorFactory = PreconfiguredConstraintValidatorFactory.builder()
				.delegated( delegatedConstraintValidatorFactory )
				.build();

		replay( delegatedConstraintValidatorFactory );

		assertThat( constraintValidatorFactory.getInstance( CountValidationCallsValidator.class ) )
				.isEqualTo( constraintValidator );

		verify( delegatedConstraintValidatorFactory );
	}

	@Test
	public void testReleaseInstance() {
		CountValidationCallsValidator constraintValidator = new CountValidationCallsValidator();

		delegatedConstraintValidatorFactory.releaseInstance( constraintValidator );

		PreconfiguredConstraintValidatorFactory constraintValidatorFactory = PreconfiguredConstraintValidatorFactory.builder()
				.delegated( delegatedConstraintValidatorFactory )
				.build();

		replay( delegatedConstraintValidatorFactory );

		constraintValidatorFactory.releaseInstance( constraintValidator );

		verify( delegatedConstraintValidatorFactory );
	}
}
