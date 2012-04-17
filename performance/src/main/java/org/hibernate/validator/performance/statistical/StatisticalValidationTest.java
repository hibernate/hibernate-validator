/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.performance.statistical;

import java.io.InputStream;
import java.util.Random;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.junit.Before;
import org.junit.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author Hardy Ferentschik
 */
public class StatisticalValidationTest {
	private static final Random random = new Random();
	private static final int NUMBER_OF_TEST_ENTITIES = 100;

	private ValidatorFactory factory;
	private Validator validator;

	@Before
	public void setUp() throws Exception {
		final Configuration<HibernateValidatorConfiguration> configuration = Validation.byProvider( HibernateValidator.class )
				.configure();

		InputStream mappingStream = StatisticalValidationTest.class.getResourceAsStream( "mapping.xml" );
		try {
			configuration.addMapping( mappingStream );
			factory = configuration.buildValidatorFactory();
			assertNotNull( factory );
		}
		finally {
			mappingStream.close();
		}

		validator = factory.getValidator();
	}

	@Test
	public void testValidationWithStatisticalGraphDepthAndConstraintValidator() throws Exception {
		for ( int i = 0; i < NUMBER_OF_TEST_ENTITIES; i++ ) {
			TestEntity entityUnderTest = new TestEntity( random, 0 );
			Set<ConstraintViolation<TestEntity>> violations = validator.validate( entityUnderTest );
			assertEquals( StatisticalConstraintValidator.threadLocalCounter.get().get(), violations.size() );
			StatisticalConstraintValidator.threadLocalCounter.get().getAndSet( 0 );
		}
	}
}



