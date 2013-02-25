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
package org.hibernate.validator.testutil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Locale;
import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstructorDescriptor;
import javax.validation.metadata.MethodDescriptor;
import javax.validation.metadata.ParameterDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import javax.validation.metadata.ReturnValueDescriptor;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;

/**
 * A helper providing useful functions for setting up validators.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class ValidatorUtil {

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
		final Configuration<HibernateValidatorConfiguration> configuration = getConfiguration();
		configuration.traversableResolver( new DummyTraversableResolver() );

		return configuration.buildValidatorFactory().getValidator();
	}

	/**
	 * Returns the {@code Configuration} object for Hibernate Validator. This method also sets the default locale to
	 * english.
	 *
	 * @return an instance of {@code Configuration} for Hibernate Validator.
	 */
	public static HibernateValidatorConfiguration getConfiguration() {
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
	public static HibernateValidatorConfiguration getConfiguration(Locale locale) {
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
	 * Returns the {@code BeanDescriptor} corresponding to the given type.
	 *
	 * @param clazz The type.
	 *
	 * @return an instance of {@code BeanDescriptor} for the given type, never {@code null}
	 */
	public static BeanDescriptor getBeanDescriptor(Class<?> clazz) {
		return getValidator().getConstraintsForClass( clazz );
	}

	/**
	 * Returns the {@code MethodDescriptor} for the given method signature in the given class.
	 *
	 * @param clazz The class.
	 * @param methodName The method name.
	 * @param parameterTypes The method parameter types.
	 *
	 * @return an instance of {@code MethodDescriptor} for the given method signature or {@code null} if no such method exists.
	 */
	public static MethodDescriptor getMethodDescriptor(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		return getBeanDescriptor( clazz ).getConstraintsForMethod( methodName, parameterTypes );
	}

	/**
	 * Returns the {@code ConstructorDescriptor} for the given constructor signature in the given class.
	 *
	 * @param clazz The class.
	 * @param parameterTypes The constructor parameter types.
	 *
	 * @return an instance of {@code ConstructorDescriptor} for the given constructor signature or {@code null} if no such constructor exists.
	 */
	public static ConstructorDescriptor getConstructorDescriptor(Class<?> clazz, Class<?>... parameterTypes) {
		return getBeanDescriptor( clazz ).getConstraintsForConstructor( parameterTypes );
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
		final MethodDescriptor methodDescriptor = getMethodDescriptor(
				clazz,
				methodName,
				parameterTypes
		);
		assertNotNull(
				methodDescriptor,
				"No method with the given signature is declared in " + clazz + " or its super class"
		);
		return methodDescriptor.getParameterDescriptors().get( parameterIndex );
	}

	/**
	 * Returns the {@code ReturnValueDescriptor} for the given method signature in the given class.
	 *
	 * @param clazz The class.
	 * @param methodName The method name.
	 * @param parameterTypes The method parameter types.
	 *
	 * @return an instance of {@code ReturnValueDescriptor} for the given method signature or {@code null} if no such method exists.
	 */
	public static ReturnValueDescriptor getMethodReturnValueDescriptor(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		MethodDescriptor methodDescriptor = getBeanDescriptor( clazz ).getConstraintsForMethod(
				methodName,
				parameterTypes
		);
		return methodDescriptor != null ? methodDescriptor.getReturnValueDescriptor() : null;
	}

	@SuppressWarnings("unchecked")
	public static <T, I extends T> T getValidatingProxy(I implementor,
														Class<?>[] interfaces,
														Validator executableValidator,
														Class<?>... validationGroups) {
		InvocationHandler handler = new ValidationInvocationHandler(
				implementor, executableValidator, validationGroups
		);

		return (T) Proxy.newProxyInstance(
				implementor.getClass().getClassLoader(),
				interfaces,
				handler
		);
	}

	/**
	 * Creates a proxy for the given object which performs a validation of the given object's method constraints upon method invocation.
	 *
	 * @param <T> The type to which the proxy shall be casted. Must be an interface.
	 * @param <I> The type of the object to be proxied.
	 * @param implementor The object to be proxied.
	 * @param executableValidator The validator to use for method validation.
	 * @param validationGroups Optionally the groups which shall be evaluated.
	 *
	 * @return A proxy performing an automatic method validation.
	 */
	public static <T, I extends T> T getValidatingProxy(I implementor, Validator executableValidator, Class<?>... validationGroups) {
		return getValidatingProxy(
				implementor,
				implementor.getClass().getInterfaces(),
				executableValidator,
				validationGroups
		);
	}
}
