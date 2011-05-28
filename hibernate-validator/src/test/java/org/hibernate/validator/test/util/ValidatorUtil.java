/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.test.util;

import java.lang.reflect.Proxy;
import java.util.Locale;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.PropertyDescriptor;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.method.MethodValidator;
import org.hibernate.validator.method.metadata.MethodDescriptor;
import org.hibernate.validator.method.metadata.ParameterDescriptor;
import org.hibernate.validator.method.metadata.TypeDescriptor;

import static org.hibernate.validator.util.Contracts.assertNotNull;

/**
 * A helper providing useful functions for setting up validators.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class ValidatorUtil {
	private static Validator hibernateValidator;

	/**
	 * Private constructor in order to avoid instantiation.
	 */
	private ValidatorUtil() {
	}

	public static Validator getValidator() {
		if ( hibernateValidator == null ) {
			Configuration<HibernateValidatorConfiguration> configuration = getConfiguration( Locale.ENGLISH );
			configuration.traversableResolver( new DummyTraversableResolver() );
			hibernateValidator = configuration.buildValidatorFactory().getValidator();
		}
		return hibernateValidator;
	}

	public static Validator getValidatorForMapping(ConstraintMapping... mappings) {
		assertNotNull( mappings );
		HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		for ( ConstraintMapping mapping : mappings ) {
			config.addMapping( mapping );
		}
		return config.buildValidatorFactory().getValidator();
	}

	public static MethodValidator getMethodValidator() {
		return getValidator().unwrap( MethodValidator.class );
	}

	public static Configuration<HibernateValidatorConfiguration> getConfiguration() {
		return getConfiguration( HibernateValidator.class, Locale.ENGLISH );
	}

	public static Configuration<HibernateValidatorConfiguration> getConfiguration(Locale locale) {
		return getConfiguration( HibernateValidator.class, locale );
	}

	public static <T extends Configuration<T>, U extends ValidationProvider<T>> T getConfiguration(Class<U> type) {
		return getConfiguration( type, Locale.ENGLISH );
	}

	public static <T extends Configuration<T>, U extends ValidationProvider<T>> T getConfiguration(Class<U> type, Locale locale) {
		Locale.setDefault( locale );
		return Validation.byProvider( type ).configure();
	}

	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String property) {
		Validator validator = getValidator();
		return validator.getConstraintsForClass( clazz ).getConstraintsForProperty( property );
	}

	public static TypeDescriptor getTypeDescriptor(Class<?> clazz) {

		TypeDescriptor typeDescriptor = getMethodValidator().getConstraintsForType( clazz );
		assertNotNull( typeDescriptor );

		return typeDescriptor;
	}

	public static MethodDescriptor getMethodDescriptor(Class<?> clazz, String methodName, Class<?>... parameterTypes) {

		MethodDescriptor methodDescriptor = getTypeDescriptor( clazz ).getConstraintsForMethod(
				methodName, parameterTypes
		);
		assertNotNull( methodDescriptor );

		return methodDescriptor;
	}

	public static ParameterDescriptor getParameterDescriptor(Class<?> clazz, String methodName, Class<?>[] parameterTypes, int parameterIndex) {

		MethodDescriptor methodDescriptor = getMethodDescriptor( clazz, methodName, parameterTypes );
		ParameterDescriptor parameterDescriptor = methodDescriptor.getParameterConstraints().get( parameterIndex );
		assertNotNull( parameterDescriptor );

		return parameterDescriptor;
	}

	public static Object getMethodValidationProxy(ValidationInvocationHandler handler) {
		final Class<?> wrappedObjectClass = handler.getWrapped().getClass();

		return Proxy.newProxyInstance(
				wrappedObjectClass.getClassLoader(),
				wrappedObjectClass.getInterfaces(),
				handler
		);
	}
}
