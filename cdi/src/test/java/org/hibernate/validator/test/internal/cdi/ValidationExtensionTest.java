/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012-2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import org.hibernate.validator.internal.cdi.ValidationExtension;
import org.hibernate.validator.internal.cdi.ValidationProviderHelper;
import org.hibernate.validator.internal.cdi.ValidatorBean;
import org.hibernate.validator.internal.cdi.ValidatorFactoryBean;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

/**
 * @author Hardy Ferentschik
 */
public class ValidationExtensionTest {
	private ValidationExtension extension;
	private AfterBeanDiscovery afterBeanDiscoveryMock;
	private ProcessBean processBeanMock;
	private Bean<ValidatorFactory> validatorFactoryBeanMock;
	private Bean<Validator> validatorBeanMock;
	private BeanManager beanManagerMock;

	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		extension = new ValidationExtension();
		afterBeanDiscoveryMock = createMock( AfterBeanDiscovery.class );
		processBeanMock = createMock( ProcessBean.class );
		beanManagerMock = createMock( BeanManager.class );
		validatorFactoryBeanMock = createMock( Bean.class );
		validatorBeanMock = createMock( Bean.class );
	}

	@Test
	public void testNullAfterBeanDiscovery() {
		try {
			extension.afterBeanDiscovery( afterBeanDiscoveryMock, null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			// success
		}
	}

	@Test
	public void testNullBeanManager() {
		try {
			extension.afterBeanDiscovery( null, beanManagerMock );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			// success
		}
	}

	@Test
	public void testNullParameters() {
		try {
			extension.afterBeanDiscovery( null, null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			// success
		}
	}

	@Test
	public void testRegisterBeanWithDefaultQualifier() {
		// setup the mocks
		ValidationProviderHelper validationProviderHelper = ValidationProviderHelper.forHibernateValidator();
		expect( processBeanMock.getBean() ).andReturn(
				new ValidatorFactoryBean(
						beanManagerMock,
						validationProviderHelper
				)
		);
		expect( processBeanMock.getBean() ).andReturn(
				new ValidatorBean(
						beanManagerMock,
						validatorFactoryBeanMock,
						validationProviderHelper
				)
		);

		afterBeanDiscoveryMock.addBean( isA( ValidatorFactoryBean.class ) );
		expectLastCall();

		afterBeanDiscoveryMock.addBean( isA( ValidatorBean.class ) );
		expectLastCall();

		// get the mocks ready
		replay( processBeanMock, afterBeanDiscoveryMock, beanManagerMock );

		// run the code
		extension.processBean( processBeanMock );
		extension.processBean( processBeanMock );
		extension.afterBeanDiscovery( afterBeanDiscoveryMock, beanManagerMock );

		// verify the mocks
		verify( processBeanMock, afterBeanDiscoveryMock, beanManagerMock );
	}

	@Test
	public void testRegisterBeanWithCustomQualifier() {
		afterBeanDiscoveryMock.addBean( isA( ValidatorFactoryBean.class ) );
		expectLastCall().andAnswer(
				new IAnswer<Object>() {

					@Override
					public Object answer() throws Throwable {
						ProcessBean<?> event = getProcessBeanEvent( (Bean<ValidatorFactory>) EasyMock.getCurrentArguments()[0] );
						extension.processBean( event );
						return null;
					}
				}
		);

		afterBeanDiscoveryMock.addBean( isA( ValidatorBean.class ) );
		expectLastCall().andAnswer(
				new IAnswer<Object>() {

					@Override
					public Object answer() throws Throwable {
						ProcessBean<?> event = getProcessBeanEvent( (Bean<ValidatorBean>) EasyMock.getCurrentArguments()[0] );
						extension.processBean( event );
						return null;
					}
				}
		);

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
		Set<Type> validatorFactoryBeanTypes = new HashSet<Type>();
		validatorFactoryBeanTypes.add( ValidatorFactory.class );

		Set<Type> validatorBeanTypes = new HashSet<Type>();
		validatorBeanTypes.add( Validator.class );

		Set<Annotation> qualifiers = Collections.<Annotation>singleton(
				new AnnotationLiteral<Default>() {
				}
		);

		expect( processBeanMock.getBean() ).andReturn( validatorFactoryBeanMock );
		expect( validatorFactoryBeanMock.getTypes() ).andReturn( validatorFactoryBeanTypes );
		expect( validatorFactoryBeanMock.getQualifiers() ).andReturn( qualifiers );
		expect( validatorFactoryBeanMock.getQualifiers() ).andReturn( qualifiers );

		expect( processBeanMock.getBean() ).andReturn( validatorBeanMock );
		expect( validatorBeanMock.getTypes() ).andReturn( validatorBeanTypes );
		expect( validatorBeanMock.getTypes() ).andReturn( validatorBeanTypes );
		expect( validatorBeanMock.getQualifiers() ).andReturn( qualifiers );
		expect( validatorBeanMock.getQualifiers() ).andReturn( qualifiers );

		// get the mocks ready
		replay( processBeanMock, validatorFactoryBeanMock, validatorBeanMock, beanManagerMock );

		// run the code
		extension.processBean( processBeanMock );
		extension.processBean( processBeanMock );

		// verify the mocks
		verify( processBeanMock, validatorFactoryBeanMock, validatorBeanMock, beanManagerMock );
	}

	@Test
	public void testProcessAnnotatedTypeNullParameter() {
		try {
			extension.processAnnotatedType( null );
			fail();
		}
		catch ( IllegalArgumentException e ) {
			// success
		}
	}

	private <T> ProcessBean<T> getProcessBeanEvent(final Bean<T> bean) {
		return new ProcessBean<T>() {

			@Override
			public Annotated getAnnotated() {
				return null;
			}

			@Override
			public Bean<T> getBean() {
				return bean;
			}

			@Override
			public void addDefinitionError(Throwable t) {
			}
		};
	}
}

