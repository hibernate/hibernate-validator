/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.engine;

import java.lang.reflect.Method;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.method.MethodConstraintViolation;

/**
 * A {@link ValidationContext} implementation which creates and manages
 * violations of type {@link MethodConstraintViolation}.
 *
 * @param <T> The type of the root bean for which this context is created.
 *
 * @author Gunnar Morling
 */
public class MethodValidationContext<T> extends ValidationContext<T, MethodConstraintViolation<T>> {

	/**
	 * The method of the current validation call.
	 */
	private final Method method;

	/**
	 * The index of the parameter to validate if this context is used for validation of a single parameter, null otherwise.
	 */
	private final Integer parameterIndex;

	protected MethodValidationContext(Class<T> rootBeanClass, T rootBean,
									  Method method,
									  MessageInterpolator messageInterpolator,
									  ConstraintValidatorFactory constraintValidatorFactory,
									  TraversableResolver traversableResolver,
									  boolean failFast) {

		this(
				rootBeanClass,
				rootBean,
				method,
				null,
				messageInterpolator,
				constraintValidatorFactory,
				traversableResolver,
				failFast
		);

	}

	protected MethodValidationContext(Class<T> rootBeanClass, T rootBean,
									  Method method,
									  Integer parameterIndex,
									  MessageInterpolator messageInterpolator,
									  ConstraintValidatorFactory constraintValidatorFactory,
									  TraversableResolver traversableResolver,
									  boolean failFast) {

		super( rootBeanClass, rootBean, messageInterpolator, constraintValidatorFactory, traversableResolver, failFast );

		this.method = method;
		this.parameterIndex = parameterIndex;
	}

	public Method getMethod() {
		return method;
	}

	public Integer getParameterIndex() {
		return parameterIndex;
	}

	@Override
	public <U, V> MethodConstraintViolation<T> createConstraintViolation(
			ValueContext<U, V> localContext, MessageAndPath messageAndPath,
			ConstraintDescriptor<?> descriptor) {

		String messageTemplate = messageAndPath.getMessage();
		String interpolatedMessage = messageInterpolator.interpolate(
				messageTemplate,
				new MessageInterpolatorContext( descriptor, localContext.getCurrentValidatedValue() )
		);
		return new MethodConstraintViolationImpl<T>(
				messageTemplate,
				interpolatedMessage,
				method,
				localContext.getParameterIndex(),
				localContext.getParameterName(),
				getRootBeanClass(),
				getRootBean(),
				localContext.getCurrentBean(),
				localContext.getCurrentValidatedValue(),
				messageAndPath.getPath(),
				descriptor,
				localContext.getElementType()
		);
	}
}
