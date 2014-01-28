/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.hibernate.validator.HibernateValidatorFactory;
import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.internal.engine.ValidatorImpl;
import org.hibernate.validator.test.internal.cdi.injection.MyValidationProvider.MyValidator;
import org.hibernate.validator.test.internal.cdi.injection.MyValidationProvider.MyValidatorFactory;
import org.hibernate.validator.testutil.TestForIssue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the injection of validator and validator factory if the default provider is not Hibernate Validator.
 *
 * @author Gunnar Morling
 */
@RunWith(Arquillian.class)
@TestForIssue(jiraKey = "HV-858")
public class InjectionOfCustomProviderTest {

	@Deployment
	public static JavaArchive createDeployment() {
		return ShrinkWrap.create( JavaArchive.class )
				.addClass( MyValidationProvider.class )
				.addAsResource(
						InjectionOfCustomProviderTest.class.getResource( "validation.xml" ),
						"META-INF/validation.xml"
				)
				.add(
						new StringAsset( "org.hibernate.validator.test.internal.cdi.injection.MyValidationProvider" ),
						"META-INF/services/javax.validation.spi.ValidationProvider"
				);
	}

	@Inject
	ValidatorFactory defaultValidatorFactory;

	@Inject
	Validator defaultValidator;

	@Inject
	MyValidatorFactory myValidatorFactory;

	@Inject
	MyValidator myValidator;

	@Inject
	@HibernateValidator
	ValidatorFactory hibernateValidatorFactory;

	@Inject
	@HibernateValidator
	Validator hibernateValidator;

	@Inject
	@HibernateValidator
	HibernateValidatorFactory hibernateValidatorSpecificFactory;

	@Test
	public void testInjectionOfDefaultFactory() throws Exception {
		assertNotNull( defaultValidatorFactory );
		assertTrue( defaultValidatorFactory instanceof MyValidatorFactory );
		assertTrue( defaultValidatorFactory.unwrap( MyValidatorFactory.class ) instanceof MyValidatorFactory );

		assertNotNull( myValidatorFactory );
	}

	@Test
	public void testInjectionOfDefaultValidator() throws Exception {
		assertNotNull( defaultValidator );
		assertTrue( defaultValidator instanceof MyValidator );
		assertNotNull( defaultValidator.forExecutables() );

		assertNotNull( myValidator );
		assertNotNull( myValidator.forExecutables() );

		assertEquals( 1, defaultValidator.validate( new TestEntity() ).size() );
		assertEquals( 1, myValidator.validate( new TestEntity() ).size() );
	}

	@Test
	public void testInjectionOfHibernateFactory() throws Exception {
		assertNotNull( hibernateValidatorFactory );
		assertNotNull( hibernateValidatorSpecificFactory );

		assertEquals( ValidatorImpl.class, hibernateValidatorFactory.getValidator().getClass() );
		assertEquals( ValidatorImpl.class, hibernateValidatorSpecificFactory.getValidator().getClass() );
	}

	@Test
	public void testInjectionOfHibernateValidator() throws Exception {
		assertNotNull( hibernateValidator );
		assertNotNull( hibernateValidator.forExecutables() );

		assertNotNull( hibernateValidator.unwrap( Validator.class ) );

		assertEquals( 1, hibernateValidator.validate( new TestEntity() ).size() );
	}

	public static class TestEntity {
		@NotNull
		private String foo;
	}
}
