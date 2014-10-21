/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ValidatorFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.integration.cdi.constraint.Pingable;
import org.hibernate.validator.integration.cdi.constraint.PingableValidator;
import org.hibernate.validator.integration.cdi.service.PingService;
import org.hibernate.validator.integration.cdi.service.PingServiceImpl;

import static org.junit.Assert.assertNotNull;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class ConstraintValidatorInjectionUnitIT {
	private static final String WAR_FILE_NAME = ConstraintValidatorInjectionUnitIT.class.getSimpleName() + ".war";

	@Inject
	@HibernateValidator
	ValidatorFactory validatorFactory;

	@Deployment
	public static WebArchive createTestArchive() throws Exception {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addClasses(
						Pingable.class,
						PingService.class,
						PingServiceImpl.class,
						PingableValidator.class
				)
				.addAsResource( "log4j.properties" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Test
	public void testSuccessfulInjectionIntoConstraintValidator() {
		ConstraintValidatorFactory constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();
		PingableValidator validator = constraintValidatorFactory.getInstance( PingableValidator.class );

		assertNotNull( "Constraint Validator could not be created", validator );
		assertNotNull( "The ping service did not get injected", validator.getPingService() );
	}
}
