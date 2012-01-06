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
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.integration.util.IntegrationTestUtil;
import org.hibernate.validator.integration.util.MyValidator;
import org.hibernate.validator.util.LazyValidatorFactory;

import static junit.framework.Assert.assertEquals;

/**
 * Tests for {@code LazyValidatorFactory}. See HV-546.
 *
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class LazyValidatorFactoryWithValidationXmlTestIT {
	private static final String WAR_FILE_NAME = LazyValidatorFactoryWithValidationXmlTestIT.class.getSimpleName() + ".war";

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create( WebArchive.class, WAR_FILE_NAME )
				.addAsLibraries( IntegrationTestUtil.bundleHibernateValidatorWithDependencies( true ) )
				.addAsLibraries( IntegrationTestUtil.createCustomBeanValidationProviderJar() )
				.addAsResource( "validation.xml", "META-INF/validation.xml" )
				.addAsResource( "log4j.properties" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" )
				.addAsWebInfResource( "jboss-deployment-structure.xml" );
	}

	@Test
	public void testBootstrapCustomProviderWithLazyFactory() throws Exception {
		LazyValidatorFactory factory = new LazyValidatorFactory();
		assertEquals(
				"The custom validator should have been created",
				MyValidator.class.getName(),
				factory.getValidator().getClass().getName()
		);
	}
}


