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
package org.hibernate.validator.internal.cdi.interceptor;


import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.InvocationContext;
import javax.validation.Validator;

/**
 * A CDI bean representing {@link ValidationInterceptor}.
 *
 * @author Gunnar Morling
 *
 */
public class ValidationInterceptorBean implements Interceptor<ValidationInterceptor> {

	private final BeanManager beanManager;
	private final Set<Annotation> interceptorBindings;
	private final Set<Type> types;

	@SuppressWarnings("serial")
	public ValidationInterceptorBean(BeanManager beanManager) {
		this.beanManager = beanManager;

		interceptorBindings = Collections.<Annotation>singleton(
				new AnnotationLiteral<MethodValidated>() {
				}
		);

		Set<Type> tmpTypes = new HashSet<Type>( 2 );
		tmpTypes.add( Object.class );
		tmpTypes.add( getBeanClass() );
		types = Collections.unmodifiableSet( tmpTypes );
	}

	@Override
	public Class<?> getBeanClass() {
		return ValidationInterceptor.class;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Collections.emptySet();
	}

	@Override
	public boolean isNullable() {
		return false;
	}

	@Override
	public ValidationInterceptor create(CreationalContext<ValidationInterceptor> creationalContext) {
		return new ValidationInterceptor( getReference( beanManager, Validator.class ) );
	}

	@SuppressWarnings("unchecked")
	private <T> T getReference(BeanManager beanManager, Class<T> clazz) {
		Set<Bean<?>> beans = beanManager.getBeans( clazz );
		for ( Bean<?> bean : beans ) {
			CreationalContext<?> context = beanManager.createCreationalContext( bean );
			return (T) beanManager.getReference( bean, clazz, context );
		}
		return null;
	}

	@Override
	public void destroy(ValidationInterceptor instance, CreationalContext<ValidationInterceptor> creationalContext) {
		creationalContext.release();
	}

	@Override
	public Set<Type> getTypes() {
		return types;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return Collections.emptySet();
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return Dependent.class;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return Collections.emptySet();
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public Set<Annotation> getInterceptorBindings() {
		return interceptorBindings;
	}

	@Override
	public boolean intercepts(InterceptionType type) {
		return EnumSet.of( InterceptionType.AROUND_CONSTRUCT, InterceptionType.AROUND_INVOKE ).contains( type );
	}

	@Override
	public Object intercept(InterceptionType type, ValidationInterceptor instance, InvocationContext ctx) throws Exception {
		switch ( type ) {
		case AROUND_CONSTRUCT:
			instance.validateConstructorInvocation( ctx );
			return null;
		case AROUND_INVOKE:
			return instance.validateMethodInvocation( ctx );
		default:
			throw new IllegalArgumentException( "Unexpected interception type: " + type );
		}
	}
}
