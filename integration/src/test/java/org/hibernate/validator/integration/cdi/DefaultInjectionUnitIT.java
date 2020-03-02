/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.integration.AbstractArquillianIT;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

/**
 * @author Hardy Ferentschik
 */
public class DefaultInjectionUnitIT extends AbstractArquillianIT {
	private static final String WAR_FILE_NAME = DefaultInjectionUnitIT.class.getSimpleName() + ".war";

	@Inject
	private ValidatorFactory validatorFactory;

	@Inject
	private BeanManager beanManager;

	@Deployment
	public static WebArchive createTestArchive() throws Exception {
		return buildTestArchive( WAR_FILE_NAME )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Test
	public void testDefaultValidatorFactoryInjected() {
		assertThat( beanManager ).as( "The bean manager should have been injected" ).isNotNull();
		assertThat( validatorFactory ).as( "The validator factory should have been injected" ).isNotNull();
	}

}
