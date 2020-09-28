/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.fail;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.validator.integration.AbstractArquillianIT;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.testng.annotations.Test;

import jakarta.validation.ValidatorFactory;

/**
 * Tests the integration of Hibernate Validator in Wildfly.
 *
 * @author Hardy Ferentschik
 */
public class JndiLookupOfValidatorFactoryIT extends AbstractArquillianIT {
	private static final String WAR_FILE_NAME = JndiLookupOfValidatorFactoryIT.class.getSimpleName() + ".war";
	private static final String DEFAULT_JNDI_NAME_OF_VALIDATOR_FACTORY = "java:comp/ValidatorFactory";

	@Deployment
	public static Archive<?> createTestArchive() {
		return buildTestArchive( WAR_FILE_NAME );
	}

	@Test
	public void testDefaultValidatorFactoryLookup() throws Exception {
		try {
			Context ctx = new InitialContext();
			Object obj = ctx.lookup( DEFAULT_JNDI_NAME_OF_VALIDATOR_FACTORY );
			assertThat( obj ).as( "The default validator factory should be bound" ).isNotNull();
			ValidatorFactory factory = (ValidatorFactory) obj;
			assertThat( factory.getValidator() )
					.as( "The Hibernate Validator implementation should be used" )
					.isExactlyInstanceOf( ValidatorImpl.class );
		}
		catch (NamingException e) {
			fail( "The default validator factory should be bound" );
		}
	}
}
