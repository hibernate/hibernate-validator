/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi.configuration;

import javax.inject.Inject;
import javax.validation.ValidatorFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.integration.cdi.service.PingService;
import org.hibernate.validator.integration.cdi.service.PingServiceImpl;

import static org.junit.Assert.assertEquals;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class ConfigurationInjectionUnitIT {
	private static final String WAR_FILE_NAME = ConfigurationInjectionUnitIT.class.getSimpleName() + ".war";

	@Inject
	@HibernateValidator
	ValidatorFactory validatorFactory;

	@Deployment
	public static WebArchive createTestArchive() throws Exception {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addClasses(
						PingService.class,
						PingServiceImpl.class,
						ConstraintValidatorFactoryWithInjection.class,
						MessageInterpolatorWithInjection.class,
						ParameterNameProviderWithInjection.class,
						TraversableResolverWithInjection.class
				)
				.addAsResource( "log4j.properties" )
				.addAsResource( "validation-custom-config.xml", "META-INF/validation.xml" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
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

	private void assertPingService(PingService pingService) {
		assertEquals( "The ping service should respond", "pong", pingService.ping() );
	}
}
