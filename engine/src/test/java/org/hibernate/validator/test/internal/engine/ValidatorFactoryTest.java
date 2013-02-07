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
package org.hibernate.validator.test.internal.engine;

import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidatorFactory;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;

import static org.testng.Assert.assertSame;

/**
 * Test for {@link ValidatorFactoryImpl}.
 *
 * @author Gunnar Morling
 */
public class ValidatorFactoryTest {

	@Test(expectedExceptions = ValidationException.class)
	public void testUnwrapToImplementationCausesValidationException() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validatorFactory.unwrap( ValidatorFactoryImpl.class );
	}

	@Test
	public void testUnwrapToPublicTypesSucceeds() {
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();

		HibernateValidatorFactory asHibernateValidatorFactory = validatorFactory.unwrap( HibernateValidatorFactory.class );
		assertSame( asHibernateValidatorFactory, validatorFactory );

		Object asObject = validatorFactory.unwrap( Object.class );
		assertSame( asObject, validatorFactory );
	}
}
