/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import javax.inject.Inject;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.hibernate.validator.integration.util.IntegrationTestUtil;
import org.hibernate.validator.integration.util.MyValidator;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

/**
 * Tests the usage of a custom validation provider coming as part of the deployment unit.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
@RunWith(Arquillian.class)
public class CustomValidationProviderInDeploymentUnitIT {

	private static final String WAR_FILE_NAME = CustomValidationProviderInDeploymentUnitIT.class.getSimpleName() + ".war";
	private static final Logger log = Logger.getLogger( CustomValidationProviderInDeploymentUnitIT.class );

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addAsLibrary( IntegrationTestUtil.createCustomBeanValidationProviderJar()
								.as( JavaArchive.class )
								.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" ) )
				.addAsResource( "log4j.properties" )
				.addAsResource( "validation-custom-provider.xml", "META-INF/validation.xml" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Inject
	private ValidatorFactory validatorFactory;

	@Test
	public void testValidatorFactoryFromCustomValidationProvider() throws Exception {
		log.debug( "Running testValidatorFactoryFromCustomValidationProvider..." );

		Validator validator = validatorFactory.getValidator();

		// Asserting the validator type as the VF is the wrapper type used within WildFly (LazyValidatorFactory)
		assertTrue(
				"The custom validator implementation as retrieved from the default provider configured in META-INF/validation.xml should be used but actually "
						+ validator + " is used",
				validator instanceof MyValidator
		);

		log.debug( "testValidatorFactoryFromCustomValidationProvider completed" );
	}
}
