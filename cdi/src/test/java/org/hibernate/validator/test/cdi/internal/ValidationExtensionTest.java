/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.cdi.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.hibernate.validator.cdi.ValidationExtension;
import org.hibernate.validator.cdi.internal.ValidationProviderHelper;
import org.hibernate.validator.cdi.internal.ValidatorBean;
import org.hibernate.validator.cdi.internal.ValidatorFactoryBean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.easymock.EasyMock;
import org.easymock.IAnswer;

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
	@BeforeEach
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
		assertThatThrownBy( () -> extension.afterBeanDiscovery( afterBeanDiscoveryMock, null ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testNullBeanManager() {
		assertThatThrownBy( () -> extension.afterBeanDiscovery( null, beanManagerMock ) )
				.isInstanceOf( IllegalArgumentException.class );
	}

	@Test
	public void testNullParameters() {
		assertThatThrownBy( () -> extension.afterBeanDiscovery( null, null ) )
				.isInstanceOf( IllegalArgumentException.class );
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
						@SuppressWarnings("unchecked")
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
						@SuppressWarnings("unchecked")
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
		assertThatThrownBy( () -> extension.processAnnotatedType( null ) )
				.isInstanceOf( IllegalArgumentException.class );
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
