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
