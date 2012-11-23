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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedCallable;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.spi.MethodValidated;

import org.easymock.Capture;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.cdi.HibernateValidator;
import org.hibernate.validator.internal.cdi.ValidationExtension;
import org.hibernate.validator.internal.cdi.ValidatorBean;
import org.hibernate.validator.internal.cdi.ValidatorFactoryBean;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.testng.Assert.assertTrue;

/**
 * @author Hardy Ferentschik
 */
public class ValidationExtensionTest {
	private ValidationExtension extension;
	private AfterBeanDiscovery afterBeanDiscoveryMock;
	private BeforeBeanDiscovery beforeBeanDiscoveryMock;
	private ProcessAnnotatedType<?> processAnnotatedTypeMock;
	private AnnotatedType<?> annotatedTypeMock;
	private AnnotatedMethod<?> annotatedMethodMock;
	private AnnotatedConstructor<?> annotatedConstructorMock;
	private BeanManager beanManagerMock;

	@BeforeMethod
	public void setUp() {
		extension = new ValidationExtension();
		afterBeanDiscoveryMock = createMock( AfterBeanDiscovery.class );
		beforeBeanDiscoveryMock = createMock( BeforeBeanDiscovery.class );
		processAnnotatedTypeMock = createMock( ProcessAnnotatedType.class );
		annotatedTypeMock = createMock( AnnotatedType.class );
		annotatedMethodMock = createMock( AnnotatedMethod.class );
		annotatedConstructorMock = createMock( AnnotatedConstructor.class );
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

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testBeforeBeanDiscoveryNullParameter() {
		extension.beforeBeanDiscovery( null, beanManagerMock );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testProcessAnnotatedTypeNullParameter() {
		extension.processAnnotatedType( null );
	}

	@Test
	public void testInterceptorBindingIsAddedProgrammatically() {
		beforeBeanDiscoveryMock.addInterceptorBinding( MethodValidated.class );

		// get the mocks ready
		replay( beforeBeanDiscoveryMock, beanManagerMock );

		// run the code
		extension.beforeBeanDiscovery( beforeBeanDiscoveryMock, beanManagerMock );

		// verify the mocks
		verify( beforeBeanDiscoveryMock, beanManagerMock );
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstrainedMethodGetsInterceptorBidingAdded() {
		AnnotationDescriptor<NotNull> descriptor = new AnnotationDescriptor<NotNull>( NotNull.class );
		Annotation notNull = AnnotationFactory.create( descriptor );
		setupMocks( annotatedMethodMock, notNull );

		Capture<AnnotatedType> capturedType = new Capture<AnnotatedType>();
		processAnnotatedTypeMock.setAnnotatedType( capture( capturedType ) );

		// get the mocks ready
		replay( processAnnotatedTypeMock, annotatedTypeMock, annotatedMethodMock, annotatedConstructorMock );

		// run the code
		extension.processAnnotatedType( processAnnotatedTypeMock );

		// verify the mocks
		verify( processAnnotatedTypeMock, annotatedTypeMock, annotatedMethodMock, annotatedConstructorMock );

		// check the captured type has @MethodValidated added
		Set<AnnotatedMethod<?>> methods = capturedType.getValue().getMethods();
		assertTrue( methods.size() == 1, "We still should only have a single method" );
		AnnotatedMethod<?> method = methods.iterator().next();
		assertTrue(
				method.isAnnotationPresent( MethodValidated.class ),
				"The @MethodValidated annotation method should have been added"
		);
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testConstrainedConstructorGetsInterceptorBidingAdded() {
		AnnotationDescriptor<NotNull> descriptor = new AnnotationDescriptor<NotNull>( NotNull.class );
		Annotation notNull = AnnotationFactory.create( descriptor );
		setupMocks( annotatedConstructorMock, notNull );

		Capture<AnnotatedType> capturedType = new Capture<AnnotatedType>();
		processAnnotatedTypeMock.setAnnotatedType( capture( capturedType ) );

		// get the mocks ready
		replay( processAnnotatedTypeMock, annotatedTypeMock, annotatedMethodMock, annotatedConstructorMock );

		// run the code
		extension.processAnnotatedType( processAnnotatedTypeMock );

		// verify the mocks
		verify( processAnnotatedTypeMock, annotatedTypeMock, annotatedMethodMock, annotatedConstructorMock );

		// check the captured type has @MethodValidated added
		Set<AnnotatedConstructor<?>> constructors = capturedType.getValue().getConstructors();
		assertTrue( constructors.size() == 1, "We still should only have a single constructor" );
		AnnotatedConstructor<?> constructor = constructors.iterator().next();
		assertTrue(
				constructor.isAnnotationPresent( MethodValidated.class ),
				"The @MethodValidated annotation method should have been added"
		);
	}

	@Test
	public void testUnConstrainedMethodDoesNotGetInterceptorBidingAdded() {
		setupMocks( annotatedMethodMock );

		// get the mocks ready
		replay( processAnnotatedTypeMock, annotatedTypeMock, annotatedMethodMock );

		// run the code
		extension.processAnnotatedType( processAnnotatedTypeMock );

		// verify the mocks
		verify( processAnnotatedTypeMock, annotatedTypeMock, annotatedMethodMock );
	}

	@SuppressWarnings("unchecked")
	private void setupMocks(AnnotatedCallable<?> callable, Annotation... constraintAnnotations) {
		expect( (AnnotatedType) processAnnotatedTypeMock.getAnnotatedType() ).andReturn( annotatedTypeMock );

		Set<AnnotatedConstructor<?>> constructors = new HashSet<AnnotatedConstructor<?>>();
		Set<Annotation> constructorAnnotations = new HashSet<Annotation>();
		if ( callable instanceof AnnotatedConstructor ) {
			constructors.add( (AnnotatedConstructor) callable );
			Collections.addAll( constructorAnnotations, constraintAnnotations );
		}
		expect( (Set<AnnotatedConstructor<?>>) annotatedTypeMock.getConstructors() ).andReturn( constructors );

		Set<AnnotatedMethod<?>> methods = new HashSet<AnnotatedMethod<?>>();
		Set<Annotation> methodAnnotations = new HashSet<Annotation>();
		if ( callable instanceof AnnotatedMethod ) {
			methods.add( (AnnotatedMethod) callable );
			Collections.addAll( methodAnnotations, constraintAnnotations );
		}
		expect( (Set<AnnotatedMethod<?>>) annotatedTypeMock.getMethods() ).andReturn( methods );

		if ( callable instanceof AnnotatedConstructor ) {
			expect( annotatedConstructorMock.getAnnotations() ).andReturn( constructorAnnotations );
		}
		else {
			expect( annotatedMethodMock.getAnnotations() ).andReturn( methodAnnotations );
		}

		if ( constraintAnnotations.length == 0 ) {
			// if there is no constraint annotation on the method the parameters get checked
			expect( (List<AnnotatedParameter<?>>) annotatedMethodMock.getParameters() ).andReturn( new ArrayList<AnnotatedParameter<?>>() );
		}
		else {
			// if we have found a constraint annotation we expect another call to getConstructors and getMethods when the wrapped type gets build
			expect( (Set<AnnotatedConstructor<?>>) annotatedTypeMock.getConstructors() ).andReturn( constructors );
			expect( (Set<AnnotatedMethod<?>>) annotatedTypeMock.getMethods() ).andReturn( methods );
		}
	}
}


