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

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.engine.ValidatorImpl;
import org.hibernate.validator.integration.util.IntegrationTestUtil;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * Tests for {@code LazyValidatorFactory}. See HV-546.
 *
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class LazyValidatorFactoryWithValidationXmlButNoProviderTestIT {
	private static final String WAR_FILE_NAME = LazyValidatorFactoryWithValidationXmlButNoProviderTestIT.class.getSimpleName() + ".war";

	@Deployment
	public static Archive<?> createTestArchive() {
		Archive<?> beanValidationJarWithMissingProvider = IntegrationTestUtil.createCustomBeanValidationProviderJar();
		Node providerClass = beanValidationJarWithMissingProvider.delete(
				"org/hibernate/validator/integration/util/MyValidationProvider.class"
		);
		if ( providerClass == null ) {
			fail( "MyValidationProvider was not as expected in the custom jar" );
		}
		return ShrinkWrap.create( WebArchive.class, WAR_FILE_NAME )
				.addClasses(
						MyValidatorCreator.class
				)
				.addAsLibraries( IntegrationTestUtil.bundleHibernateValidatorWithDependencies( true ) )
				.addAsLibrary( beanValidationJarWithMissingProvider )
				.addAsResource( "log4j.properties" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, ArchivePaths.create( "beans.xml" ) )
				.addAsWebInfResource( "jboss-deployment-structure.xml" );
	}

	@Inject
	private MyValidatorCreator validatorCreator;

	@Test
	public void testBootstrappingDoesNotFailDueToMissingCustomProvider() throws Exception {
		assertNotNull( "The creator bean should have been injected", validatorCreator );
		assertEquals(
				"Since the custom provider cannot be loaded, Hibernate Validator should be the default",
				ValidatorImpl.class.getName(),
				validatorCreator.getValidator().getClass().getName()
		);
	}
}


