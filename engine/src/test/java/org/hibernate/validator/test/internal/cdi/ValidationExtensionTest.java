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
package org.hibernate.validator.test.internal.cdi;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.internal.cdi.ValidationExtension;
import org.hibernate.validator.internal.cdi.ValidatorBean;
import org.hibernate.validator.internal.cdi.ValidatorFactoryBean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

/**
 * @author Hardy Ferentschik
 */
public class ValidationExtensionTest {
	private ValidationExtension extension;
	private AfterBeanDiscovery afterBeanDiscoveryMock;
	private BeanManager beanManagerMock;

	@BeforeMethod
	public void setUp() {
		extension = new ValidationExtension();
		afterBeanDiscoveryMock = createMock( AfterBeanDiscovery.class );
		beanManagerMock = createMock( BeanManager.class );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullAfterBeanDiscovery() {
		extension.afterBeanDiscovery( afterBeanDiscoveryMock, null );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullBeanManager() {
		extension.afterBeanDiscovery( null, beanManagerMock );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testNullParameters() {
		extension.afterBeanDiscovery( null, null );
	}

	@Test
	public void testRegisterBeanWithDefaultQualifier() {
		// setup the mocks
		Set<Annotation> qualifiers = new HashSet<Annotation>();
		qualifiers.add(
				new AnnotationLiteral<HibernateValidator>() {
				}
		);
		Set<Bean<?>> beans = new HashSet<Bean<?>>();
		beans.add( new ValidatorBean( beanManagerMock, qualifiers ) );
		beans.add( new ValidatorFactoryBean( beanManagerMock, qualifiers ) );
		expect( beanManagerMock.getBeans( ValidatorFactory.class ) ).andReturn( beans );
		afterBeanDiscoveryMock.addBean( isA( ValidatorFactoryBean.class ) );

		expect( beanManagerMock.getBeans( Validator.class ) ).andReturn( beans );
		afterBeanDiscoveryMock.addBean( isA( ValidatorBean.class ) );

		// get the mocks ready
		replay( afterBeanDiscoveryMock, beanManagerMock );

		// run the code
		extension.afterBeanDiscovery( afterBeanDiscoveryMock, beanManagerMock );

		// verify the mocks
		verify( afterBeanDiscoveryMock, beanManagerMock );
	}

	@Test
	public void testRegisterBeanWithCustomQualifier() {
		// setup the mocks
		expect( beanManagerMock.getBeans( ValidatorFactory.class ) ).andReturn( new HashSet<Bean<?>>() );
		afterBeanDiscoveryMock.addBean( isA( ValidatorFactoryBean.class ) );

		expect( beanManagerMock.getBeans( Validator.class ) ).andReturn( new HashSet<Bean<?>>() );
		afterBeanDiscoveryMock.addBean( isA( ValidatorBean.class ) );

		// get the mocks ready
		replay( afterBeanDiscoveryMock, beanManagerMock );

		// run the code
		extension.afterBeanDiscovery( afterBeanDiscoveryMock, beanManagerMock );

		// verify the mocks
		verify( afterBeanDiscoveryMock, beanManagerMock );
	}

	@Test
	public void testNoRegistrationRequired() {
		// setup the mocks
		Set<Annotation> qualifiers = new HashSet<Annotation>();
		qualifiers.add(
				new AnnotationLiteral<HibernateValidator>() {
				}
		);
		qualifiers.add(
				new AnnotationLiteral<Default>() {
				}
		);
		Set<Bean<?>> beans = new HashSet<Bean<?>>();
		beans.add( new ValidatorBean( beanManagerMock, qualifiers ) );
		beans.add( new ValidatorFactoryBean( beanManagerMock, qualifiers ) );
		expect( beanManagerMock.getBeans( ValidatorFactory.class ) ).andReturn( beans );
		expect( beanManagerMock.getBeans( Validator.class ) ).andReturn( beans );

		// get the mocks ready
		replay( afterBeanDiscoveryMock, beanManagerMock );

		// run the code
		extension.afterBeanDiscovery( afterBeanDiscoveryMock, beanManagerMock );

		// verify the mocks
		verify( afterBeanDiscoveryMock, beanManagerMock );
	}
}


