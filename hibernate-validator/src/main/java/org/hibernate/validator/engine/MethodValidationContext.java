// $Id: MethodValidationContext.java 19033 Oct 1, 2010 6:11:08 PM gunnar.morling $
/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
import java.util.List;

import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.MethodConstraintViolation;

/**
 * @author Gunnar Morling
 *
 */
public class MethodValidationContext <T> extends ValidationContext<T> {

	private final Method method;
	
	private final Integer parameterIndex;
	
	public MethodValidationContext(
		Method method, Integer parameterIndex, Class<T> rootBeanClass, T rootBean,
			MessageInterpolator messageInterpolator,
			ConstraintValidatorFactory constraintValidatorFactory,
			TraversableResolver traversableResolver) {
		super(rootBeanClass, rootBean, messageInterpolator, constraintValidatorFactory,
				traversableResolver);
		
		this.method = method;
		this.parameterIndex = parameterIndex;
	}
	
	public final Method getMethod() {
		return method;
	}

	public final Integer getParameterIndex() {
		return parameterIndex;
	}

	public <U, V> MethodConstraintViolation<T> createConstraintViolation(ValueContext<U, V> localContext, MessageAndPath messageAndPath, ConstraintDescriptor<?> descriptor) {
		String messageTemplate = messageAndPath.getMessage();
		String interpolatedMessage = getMessageInterpolator().interpolate(
				messageTemplate,
				new MessageInterpolatorContext( descriptor, localContext.getCurrentValidatedValue() )
		);
		
		return new MethodConstraintViolationImpl<T>(
					messageTemplate,
					interpolatedMessage,
					method,
					parameterIndex,
					getRootBeanClass(),
					getRootBean(),
					localContext.getCurrentBean(),
					localContext.getCurrentValidatedValue(),
					PathImpl.createPathFromString(messageAndPath.getPath()),
					descriptor,
					localContext.getElementType()
			);
	}
	
	public List<MethodConstraintViolation<T>> getFailingConstraints() {
		return (List<MethodConstraintViolation<T>>) super.getFailingConstraints();
	}
}
