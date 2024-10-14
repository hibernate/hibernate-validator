/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.integration.wildfly;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.integration.AbstractArquillianIT;
import org.hibernate.validator.integration.util.IntegrationTestUtil;
import org.hibernate.validator.integration.util.MyValidator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import org.testng.annotations.Test;

/**
 * Tests the usage of a custom validation provider coming as part of the deployment unit.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class CustomValidationProviderInDeploymentUnitIT extends AbstractArquillianIT {

	private static final String WAR_FILE_NAME = CustomValidationProviderInDeploymentUnitIT.class.getSimpleName() + ".war";

	@Deployment
	public static Archive<?> createTestArchive() {
		return buildTestArchive( WAR_FILE_NAME )
				.addAsLibrary( IntegrationTestUtil.createCustomBeanValidationProviderJar()
						.as( JavaArchive.class )
						.addAsManifestResource( BEANS_XML, "beans.xml" ) )
				.addAsResource( "log4j.properties" )
				.addAsResource( "validation-custom-provider.xml", "META-INF/validation.xml" )
				.addAsWebInfResource( BEANS_XML, "beans.xml" );
	}

	@Inject
	private ValidatorFactory validatorFactory;

	@Test
	public void testValidatorFactoryFromCustomValidationProvider() throws Exception {
		Validator validator = validatorFactory.getValidator();

		// Asserting the validator type as the VF is the wrapper type used within WildFly (LazyValidatorFactory)
		assertThat( validator )
				.as( "The custom validator implementation as retrieved from the default provider configured in META-INF/validation.xml should be used but actually "
						+ validator + " is used" )
				.isInstanceOf( MyValidator.class );
	}
}
