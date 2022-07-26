/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.referenceguide.chapter06.customvalidatorwithdependency;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.mock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.testutil.PreconfiguredValidatorsValidatorFactory;

import org.junit.Test;

@SuppressWarnings("unused")
//tag::field[]
public class CustomValidatorWithDependencyTest {

	@Test
	public void mockCustomValidatorWithDependency() {
		ZipCodeValidator zipCodeValidator = mock( ZipCodeValidator.class );

		expect( zipCodeValidator.isValid( eq( "1234" ), isA( ConstraintValidatorContext.class ) ) )
				.andStubReturn( true );
		zipCodeValidator.initialize( isA( ZipCode.class ) );

		replay( zipCodeValidator );

		ValidatorFactory validatorFactory = PreconfiguredValidatorsValidatorFactory.builder()
				.defaultValidators( Map.of( ZipCodeValidator.class, zipCodeValidator ) )
				.build();

		Validator validator = validatorFactory.getValidator();

		Person person = new Person( "1234" );

		Set<ConstraintViolation<Person>> constraintViolations = validator.validate( person );

		assertEquals( 0, constraintViolations.size() );

		verify( zipCodeValidator );
	}
}
//end::field[]
