/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.cfg;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;

/**
 * Via instances of this class method constraints can be configured for a single bean class.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class ConstraintsForTypeMethod {
	private final ConstraintMapping mapping;
	private final Class<?> beanClass;
	private final String methodName;
	private final Class<?>[] parameterTypes;

	public ConstraintsForTypeMethod(Class<?> beanClass, String methodName, Class<?>[] parameterTypes, ConstraintMapping mapping) {
		this.beanClass = beanClass;
		this.methodName = methodName;
		this.mapping = mapping;
		this.parameterTypes = parameterTypes;
	}

	/**
	 * Defines constraints on the return value of the current method.
	 *
	 * @return Returns a new {@code ConstraintsForTypeMethodElement} instance allowing method chaining.
	 */
	public ConstraintsForTypeMethodElement returnValue() {
		return new ConstraintsForTypeMethodElement( beanClass, methodName, parameterTypes, METHOD, mapping );
	}

	/**
	 * Changes the parameter for which added constraints apply.
	 *
	 * @param index The parameter index.
	 *
	 * @return Returns a new {@code ConstraintsForTypeMethodElement} instance allowing method chaining.
	 */
	public ConstraintsForTypeMethodElement parameter(int index) {
		return new ConstraintsForTypeMethodElement( beanClass, methodName, parameterTypes, PARAMETER, index, mapping );
	}
}



