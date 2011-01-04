/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.test.engine.methodlevel;

import static org.hibernate.validator.test.util.TestUtil.assertConstraintViolation;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.lang.reflect.Proxy;

import javax.validation.Validation;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.MethodConstraintViolation;
import org.hibernate.validator.MethodConstraintViolationException;
import org.hibernate.validator.MethodValidator;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepositoryWithRedefinedDefaultGroup;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepositoryWithRedefinedDefaultGroup.ValidationGroup1;
import org.hibernate.validator.test.engine.methodlevel.service.CustomerRepositoryWithRedefinedDefaultGroupImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Integration test for the group sequence processing during method-level validation.
 *
 * @author Gunnar Morling
 */
public class MethodLevelValidationGroupSequenceTest {

	private CustomerRepositoryWithRedefinedDefaultGroup customerRepository;

	@BeforeMethod
	public void setUpMethodValidator() {

		MethodValidator validator = Validation.byProvider( HibernateValidator.class )
				.configure()
				.allowMethodLevelConstraints()
				.buildValidatorFactory()
				.getValidator()
				.unwrap( MethodValidator.class );

		customerRepository = ( CustomerRepositoryWithRedefinedDefaultGroup ) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class<?>[] { CustomerRepositoryWithRedefinedDefaultGroup.class },
				new ValidationInvocationHandler( new CustomerRepositoryWithRedefinedDefaultGroupImpl(), validator )
		);
	}

	@Test
	public void validationSucceedsAsNoConstraintInDefaultSequenceIsViolated() {
		customerRepository.noConstraintInDefaultGroup( null );
	}

	@Test
	public void validationFailsAsConstraintInDefaultSequenceIsViolated() {

		try {
			customerRepository.constraintInDefaultGroup( null );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertConstraintViolation(
					constraintViolation, "may not be null", CustomerRepositoryWithRedefinedDefaultGroupImpl.class, null
			);
			assertEquals(
					constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup1.class
			);
		}
	}

	/**
	 * Only one constraint violation is expected, as processing should stop after the
	 * first erroneous group of the default sequence.
	 */
	@Test
	public void processingOfDefaultSequenceStopsAfterFirstErroneousGroup() {

		try {
			customerRepository.constraintInLaterPartOfDefaultSequence( 1 );
			fail( "Expected MethodConstraintViolationException wasn't thrown." );
		}
		catch ( MethodConstraintViolationException e ) {

			assertEquals( e.getConstraintViolations().size(), 1 );

			MethodConstraintViolation<?> constraintViolation = e.getConstraintViolations().iterator().next();
			assertConstraintViolation(
					constraintViolation,
					"must be greater than or equal to 5",
					CustomerRepositoryWithRedefinedDefaultGroupImpl.class,
					1
			);
			assertEquals(
					constraintViolation.getConstraintDescriptor().getGroups().iterator().next(), ValidationGroup1.class
			);
		}
	}
}
