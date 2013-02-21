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

import javax.inject.Inject;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;

import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.integration.util.IntegrationTestUtil;

import static junit.framework.Assert.assertNotNull;

/**
 * @author Hardy Ferentschik
 */
//TODO HV-723 Re-enable
//@RunWith(Arquillian.class)
public class QualifiedInjectionUnitIT {
	private static final String WAR_FILE_NAME = QualifiedInjectionUnitIT.class.getSimpleName() + ".war";

	@Inject
	@HibernateValidator
	private ValidatorFactory validatorFactory;

	@Inject
	@HibernateValidator
	private Validator validator;

	@Deployment
	public static WebArchive createTestArchive() throws Exception {
		return ShrinkWrap
				.create( WebArchive.class, WAR_FILE_NAME )
				.addAsLibraries( IntegrationTestUtil.bundleHibernateValidatorWithDependencies( false ) )
				.addAsLibraries( IntegrationTestUtil.bundleLoggingDependencies() )
				.addAsLibraries( IntegrationTestUtil.bundleOptionalDependencies() )
				.addAsResource( "log4j.properties" )
				.addAsWebInfResource( "jboss-deployment-structure.xml" )
				.addAsWebInfResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	//TODO HV-723 Re-enable
	@Test
	@Ignore
	public void testQualifiedValidatorFactoryAndValidatorInjectable() {
		assertNotNull( "The validator factory should have been injected", validatorFactory );
		assertNotNull( "The validator should have been injected", validator );
	}
}
