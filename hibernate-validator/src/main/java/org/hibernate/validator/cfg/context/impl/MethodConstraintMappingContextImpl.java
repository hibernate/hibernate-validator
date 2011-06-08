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
package org.hibernate.validator.cfg.context.impl;

import java.lang.reflect.Method;

import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;

/**
 * A constraint mapping creational context which allows to select the parameter or
 * return value to which the next operations shall apply.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 * @author Gunnar Morling
 */
public class MethodConstraintMappingContextImpl implements MethodConstraintMappingContext {

	private final Class<?> beanClass;
	private final Method method;
	private final ConstraintMappingContext mapping;

	public MethodConstraintMappingContextImpl(Class<?> beanClass, Method method, ConstraintMappingContext mapping) {

		this.beanClass = beanClass;
		this.method = method;
		this.mapping = mapping;
	}

	public ParameterConstraintMappingContext parameter(int index) {
		return new ParameterConstraintMappingContextImpl(
				beanClass, method, index, mapping
		);
	}

	public ReturnValueConstraintMappingContext returnValue() {
		return new ReturnValueConstraintMappingContextImpl(
				beanClass, method, mapping
		);
	}

}
