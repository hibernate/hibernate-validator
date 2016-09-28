/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.performance.statistical;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Set;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class StatisticalValidationTest {
	private static final int NUMBER_OF_TEST_ENTITIES = 100;

	private static Validator validator;
	private static TestEntity[] entitiesUnderTest = new TestEntity[NUMBER_OF_TEST_ENTITIES];

	@BeforeTest
	public static void setUpValidatorFactory() throws Exception {
		ValidatorFactory factory;
		final Configuration<?> configuration = Validation.byDefaultProvider().configure();
		InputStream mappingStream = StatisticalValidationTest.class.getResourceAsStream( "mapping.xml" );
		try {
			configuration.addMapping( mappingStream );
			factory = configuration.buildValidatorFactory();
			assertThat( factory ).isNotNull();
		}
		finally {
			mappingStream.close();
		}

		validator = factory.getValidator();

		for ( int i = 0; i < NUMBER_OF_TEST_ENTITIES; i++ ) {
			entitiesUnderTest[i] = new TestEntity( i % 10 );
		}
	}

	@Test
	public void testValidationWithStatisticalGraphDepthAndConstraintValidator() throws Exception {
		for ( int i = 0; i < NUMBER_OF_TEST_ENTITIES; i++ ) {
			Set<ConstraintViolation<TestEntity>> violations = validator.validate( entitiesUnderTest[i] );
			assertThat( violations ).hasSize( StatisticalConstraintValidator.threadLocalCounter.get().getFailures() );
			StatisticalConstraintValidator.threadLocalCounter.get().reset();
		}
	}
}



