/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.integration.AbstractArquillianIT;
import org.hibernate.validator.integration.cdi.constraint.Pingable;
import org.hibernate.validator.integration.cdi.constraint.PingableValidator;
import org.hibernate.validator.integration.cdi.service.PingService;
import org.hibernate.validator.integration.cdi.service.PingServiceImpl;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintValidatorInjectionUnitIT extends AbstractArquillianIT {
	private static final String WAR_FILE_NAME = ConstraintValidatorInjectionUnitIT.class.getSimpleName() + ".war";

	@Inject
	@HibernateValidator
	ValidatorFactory validatorFactory;

	@Deployment
	public static WebArchive createTestArchive() throws Exception {
		return buildTestArchive( WAR_FILE_NAME )
				.addClasses(
						Pingable.class,
						PingService.class,
						PingServiceImpl.class,
						PingableValidator.class
				)
				.addAsWebInfResource( BEANS_XML, "beans.xml" );
	}

	@Test
	public void testSuccessfulInjectionIntoConstraintValidator() {
		ConstraintValidatorFactory constraintValidatorFactory = validatorFactory.getConstraintValidatorFactory();
		PingableValidator validator = constraintValidatorFactory.getInstance( PingableValidator.class );

		assertThat( validator ).as( "Constraint Validator could not be created" ).isNotNull();
		assertThat( validator.getPingService() ).as( "The ping service did not get injected" ).isNotNull();
	}
}
