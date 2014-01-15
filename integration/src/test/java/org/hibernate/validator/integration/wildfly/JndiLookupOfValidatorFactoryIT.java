/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.integration.wildfly;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.ValidatorFactory;

import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the integration of Hibernate Validator in Wildfly.
 *
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class JndiLookupOfValidatorFactoryIT {
	private static final String WAR_FILE_NAME = JndiLookupOfValidatorFactoryIT.class.getSimpleName() + ".war";
	private static final Logger log = Logger.getLogger( JndiLookupOfValidatorFactoryIT.class );
	private static final String DEFAULT_JNDI_NAME_OF_VALIDATOR_FACTORY = "java:comp/ValidatorFactory";

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addAsResource( "log4j.properties" );
	}

	@Test
	public void testDefaultValidatorFactoryLookup() throws Exception {
		log.debug( "Running testDefaultValidatorFactoryLookup..." );
		try {
			Context ctx = new InitialContext();
			Object obj = ctx.lookup( DEFAULT_JNDI_NAME_OF_VALIDATOR_FACTORY );
			assertTrue( "The default validator factory should be bound", obj != null );
			ValidatorFactory factory = (ValidatorFactory) obj;
			assertEquals(
					"The Hibernate Validator implementation should be used",
					"ValidatorImpl",
					factory.getValidator().getClass().getSimpleName()
			);
		}
		catch( NamingException e ) {
			fail( "The default validator factory should be bound" );
		}
		log.debug( "testDefaultValidatorFactoryLookup completed" );
	}
}
