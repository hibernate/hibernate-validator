/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.cdi;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.validation.ValidatorFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class DefaultInjectionUnitIT {
	private static final String WAR_FILE_NAME = DefaultInjectionUnitIT.class.getSimpleName() + ".war";

	@Inject
	private ValidatorFactory validatorFactory;

	@Inject
	private BeanManager beanManager;

	@Deployment
	public static WebArchive createTestArchive() throws Exception {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@Test
	public void testDefaultValidatorFactoryInjected() {
		assertNotNull( "The bean manager should have been injected", beanManager );
		assertNotNull( "The validator factory should have been injected", validatorFactory );
	}

}
