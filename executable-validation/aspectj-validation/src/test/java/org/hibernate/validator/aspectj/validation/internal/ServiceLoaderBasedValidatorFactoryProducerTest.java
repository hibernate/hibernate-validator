/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.aspectj.validation.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ServiceLoader;

import javax.validation.ValidatorFactory;

import org.testng.annotations.Test;

/**
 * @author Marko Bekhta
 */
public class ServiceLoaderBasedValidatorFactoryProducerTest {

	/**
	 * Test that producer is correctly loaded by {@link ServiceLoader} by checking the type of configured clock provider.
	 */
	@Test
	public void testGetValidatorFactory() throws Exception {
		ServiceLoaderBasedValidatorFactoryProducer producer = new ServiceLoaderBasedValidatorFactoryProducer();
		ValidatorFactory validatorFactory = producer.getValidatorFactory();
		assertThat( validatorFactory.getClockProvider() ).isInstanceOf( ValidatorFactoryProducerTestImpl.FixedClockProvider.class );
	}
}
