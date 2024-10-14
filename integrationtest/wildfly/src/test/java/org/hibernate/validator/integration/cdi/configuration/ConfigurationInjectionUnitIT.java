/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.cdi.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.integration.AbstractArquillianIT;
import org.hibernate.validator.integration.cdi.service.PingService;
import org.hibernate.validator.integration.cdi.service.PingServiceImpl;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class ConfigurationInjectionUnitIT extends AbstractArquillianIT {
	private static final String WAR_FILE_NAME = ConfigurationInjectionUnitIT.class.getSimpleName() + ".war";

	@Inject
	@HibernateValidator
	ValidatorFactory validatorFactory;

	@Deployment
	public static WebArchive createTestArchive() throws Exception {
		return buildTestArchive( WAR_FILE_NAME )
				.addClasses(
						PingService.class,
						PingServiceImpl.class,
						ConstraintValidatorFactoryWithInjection.class,
						MessageInterpolatorWithInjection.class,
						ParameterNameProviderWithInjection.class,
						TraversableResolverWithInjection.class,
						ClockProviderWithInjection.class
				)
				.addAsResource( "validation-custom-config.xml", "META-INF/validation.xml" )
				.addAsWebInfResource( BEANS_XML, "beans.xml" );
	}

	@Test
	public void testConstraintValidatorFactoryGotInjected() {
		ConstraintValidatorFactoryWithInjection constraintValidatorFactory = (ConstraintValidatorFactoryWithInjection) validatorFactory
				.getConstraintValidatorFactory();

		assertPingService( constraintValidatorFactory.getPingService() );
	}

	@Test
	public void testMessageInterpolatorGotInjected() {
		MessageInterpolatorWithInjection messageInterpolator = (MessageInterpolatorWithInjection) validatorFactory
				.getMessageInterpolator();

		assertPingService( messageInterpolator.getPingService() );
	}

	@Test
	public void testTraversableResolverGotInjected() {
		TraversableResolverWithInjection traversableResolver = (TraversableResolverWithInjection) validatorFactory
				.getTraversableResolver();

		assertPingService( traversableResolver.getPingService() );
	}

	@Test
	public void testClockProviderGotInjected() {
		ClockProviderWithInjection clockProvider = (ClockProviderWithInjection) validatorFactory.getClockProvider();

		assertPingService( clockProvider.getPingService() );
	}

	private void assertPingService(PingService pingService) {
		assertThat( pingService.ping() ).as( "The ping service should respond" ).isEqualTo( "pong" );
	}
}
