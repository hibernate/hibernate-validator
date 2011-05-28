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

	/**
	 * Returns a configured instance of {@code Validator}. This validator is configured to use a
	 * {@link DummyTraversableResolver}. This method also sets the default locale to english.
	 *
	 * @return an instance of {@code Validator}.
	 */
	public static Validator getValidator() {
		if ( hibernateValidator == null ) {
			final Configuration<HibernateValidatorConfiguration> configuration = getConfiguration();
			configuration.traversableResolver( new DummyTraversableResolver() );
			hibernateValidator = configuration.buildValidatorFactory().getValidator();
		}
		return hibernateValidator;
	}

	/**
	 * Returns a configured instance of {@code Validator} initialized with the given constraint mappings. This validator
	 * is configured to use a {@link DummyTraversableResolver}. This method also sets the default locale to english.
	 *
	 * @param mappings The constraint mappings.
	 *
	 * @return an instance of {@code Validator}.
	 */
	public static Validator getValidatorForProgrammaticMapping(ConstraintMapping... mappings) {
		assertNotNull( mappings );
		final HibernateValidatorConfiguration config = getConfiguration( HibernateValidator.class );
		config.traversableResolver( new DummyTraversableResolver() );
		for ( ConstraintMapping mapping : mappings ) {
			config.addMapping( mapping );
		}
		return config.buildValidatorFactory().getValidator();
	}

	/**
	 * Returns an instance of {@code MethodValidator} which can be used to validate method constraints. This validator
	 * is constructed by getting an instance of {@code Validator} with the method {@link ValidatorUtil#getValidator()}.
	 *
	 * @return an instance of {@code MethodValidator}.
	 */
	public static MethodValidator getMethodValidator() {
		return getValidator().unwrap( MethodValidator.class );
	}

	/**
	 * Returns the {@code Configuration} object for Hibernate Validator. This method also sets the default locale to
	 * english.
	 *
	 * @return an instance of {@code Configuration} for Hibernate Validator.
	 */
	public static Configuration<HibernateValidatorConfiguration> getConfiguration() {
		return getConfiguration( HibernateValidator.class, Locale.ENGLISH );
	}

	/**
	 * Returns the {@code Configuration} object for Hibernate Validator. This method also sets the default locale to
	 * the given locale.
	 *
	 * @param locale The default locale to set.
	 *
	 * @return an instance of {@code Configuration} for Hibernate Validator.
	 */
	public static Configuration<HibernateValidatorConfiguration> getConfiguration(Locale locale) {
		return getConfiguration( HibernateValidator.class, locale );
	}

	/**
	 * Returns the {@code Configuration} object for the given validation provider type. This method also sets the
	 * default locale to english.
	 *
	 * @param type The validation provider type.
	 *
	 * @return an instance of {@code Configuration}.
	 */
	public static <T extends Configuration<T>, U extends ValidationProvider<T>> T getConfiguration(Class<U> type) {
		return getConfiguration( type, Locale.ENGLISH );
	}

	/**
	 * Returns the {@code Configuration} object for the given validation provider type. This method also sets the
	 * default locale to the given locale.
	 *
	 * @param type The validation provider type.
	 * @param locale The default locale to set.
	 *
	 * @return an instance of {@code Configuration}.
	 */
	public static <T extends Configuration<T>, U extends ValidationProvider<T>> T getConfiguration(Class<U> type, Locale locale) {
		Locale.setDefault( locale );
		return Validation.byProvider( type ).configure();
	}

	/**
	 * Returns the {@code PropertyDescriptor} corresponding to the property contained in the given class.
	 *
	 * @param clazz The class containing the given property.
	 * @param property The property name.
	 *
	 * @return an instance of {@code PropertyDescriptor} or {@code null} if does not exists or has no constraint.
	 *
	 * @see javax.validation.metadata.BeanDescriptor#getConstraintsForProperty(String)
	 */
	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String property) {
		return getValidator().getConstraintsForClass( clazz ).getConstraintsForProperty( property );
	}

	/**
	 * Returns the {@code TypeDescriptor} corresponding to the given type.
	 *
	 * @param clazz The type.
	 *
	 * @return an instance of {@code TypeDescriptor} for the given type, never {@code null}
	 */
	public static TypeDescriptor getTypeDescriptor(Class<?> clazz) {
		return getMethodValidator().getConstraintsForType( clazz );
	}

	/**
	 * Returns the {@code MethodDescriptor} for the given method signature in the given class.
	 *
	 * @param clazz The class.
	 * @param methodName The method name.
	 * @param parameterTypes The method parameter types.
	 *
	 * @return an instance of {@code MethodDescriptor} for the given method signature or {@code null} if does not exists.
	 *
	 * @see TypeDescriptor#getConstraintsForMethod(String, Class[])
	 */
	public static MethodDescriptor getMethodDescriptor(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return getTypeDescriptor( clazz ).getConstraintsForMethod( methodName, parameterTypes );
	}

	/**
	 * Returns the {@code ParameterDescriptor} for the parameter at the given index within the method, corresponding to
	 * the given signature, within the given class.
	 *
	 * @param clazz The class.
	 * @param methodName The method name.
	 * @param parameterTypes The method parameter types.
	 * @param parameterIndex The parameter index.
	 *
	 * @return an instance of {@code ParameterDescriptor}.
	 */
	public static ParameterDescriptor getParameterDescriptor(Class<?> clazz, String methodName, Class<?>[] parameterTypes, int parameterIndex) {
		final MethodDescriptor methodDescriptor = getMethodDescriptor( clazz, methodName, parameterTypes );
		assertNotNull( methodDescriptor );
		return methodDescriptor.getParameterConstraints().get( parameterIndex );
	}

	/**
	 * Returns an instance of proxy which dispatches method invocation to the given validation invocation handler.
	 *
	 * @param handler The validation invocation handler.
	 *
	 * @return a proxy instance configured with the given handler.
	 *
	 * @see ValidationInvocationHandler
	 */
	public static Object getMethodValidationProxy(ValidationInvocationHandler handler) {
		final Class<?> wrappedObjectClass = handler.getWrapped().getClass();
		return Proxy.newProxyInstance(
				wrappedObjectClass.getClassLoader(),
				wrappedObjectClass.getInterfaces(),
				handler
		);
	}
}
