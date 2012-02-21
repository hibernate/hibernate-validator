/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.integration.lazyfactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.impl.engine.ValidatorImpl;
import org.hibernate.validator.impl.util.LazyValidatorFactory;
import org.hibernate.validator.integration.util.IntegrationTestUtil;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for {@code LazyValidatorFactory}. See HV-546.
 *
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class LazyValidatorFactoryWithoutValidationXmlTestIT {
	private static final String WAR_FILE_NAME = LazyValidatorFactoryWithoutValidationXmlTestIT.class.getSimpleName() + ".war";

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create( WebArchive.class, WAR_FILE_NAME )
				.addAsLibraries( IntegrationTestUtil.bundleHibernateValidatorWithDependencies( true ) )
				.addAsLibrary( IntegrationTestUtil.createCustomBeanValidationProviderJar() )
				.addAsResource( "log4j.properties" )
				.addAsWebInfResource( "jboss-deployment-structure.xml" );
	}

	@Test
	public void testBootstrapWithoutValidationXmlCreatesHibernateValidatorInstance() throws Exception {
		LazyValidatorFactory factory = new LazyValidatorFactory();
		assertEquals(
				"Hibernate Validator should be the chosen provider. " +
						"Even though we bundle another provider it does not get explicitly configured.",
				ValidatorImpl.class.getName(),
				factory.getValidator().getClass().getName()
		);
	}
}


