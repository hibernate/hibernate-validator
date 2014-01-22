/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.internal.cdi.injection;

import javax.inject.Inject;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cdi.HibernateValidator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Hardy Ferentschik
 */
@RunWith(Arquillian.class)
public class InjectionTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addAsManifestResource( EmptyAsset.INSTANCE, "beans.xml" );
	}

	@HibernateValidator
	@Inject
	ValidatorFactory validatorFactory;

	@HibernateValidator
	@Inject
	Validator validator;

	@Inject
	ValidatorFactory defaultValidatorFactory;

	@Inject
	Validator defaultValidator;

	@Inject
	HibernateValidatorFactory hibernateValidatorFactory;

	@Test
	public void testInjectionOfQualifiedBeans() throws Exception {
		assertNotNull( validatorFactory );
		assertNotNull( validator );

		assertEquals( 1, validator.validate( new TestEntity() ).size() );
	}

	@Test
	public void testInjectionOfDefaultBeans() throws Exception {
		assertNotNull( defaultValidatorFactory );
		assertNotNull( defaultValidator );

		assertEquals( 1, defaultValidator.validate( new TestEntity() ).size() );
	}

	@Test
	public void testInjectionOfHibernateValidatorFactory() throws Exception {
		assertNotNull( hibernateValidatorFactory );
		assertEquals( 1, hibernateValidatorFactory.getValidator().validate( new TestEntity() ).size() );
	}

	public static class TestEntity {
		@NotNull
		private String foo;
	}
}
