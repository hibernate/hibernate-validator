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
package org.hibernate.validator.cfg.context.impl;

import java.lang.reflect.Method;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;
import org.hibernate.validator.metadata.location.MethodConstraintLocation;

/**
 * Constraint mapping creational context which allows to configure the constraints for one method return value.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class ReturnValueConstraintMappingContextImpl
		extends ConstraintMappingContextImplBase
		implements ReturnValueConstraintMappingContext {

	private final Method method;

	public ReturnValueConstraintMappingContextImpl(Class<?> beanClass, Method method, ConstraintMappingContext mapping) {

		super( beanClass, mapping );

		this.method = method;
	}

	public ReturnValueConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		mapping.addMethodConstraintConfig(
				ConfiguredConstraint.forReturnValue(
						definition, method
				)
		);
		return this;
	}

	/**
	 * Marks the current property as cascadable.
	 *
	 * @return Returns itself for method chaining.
	 */
	public ReturnValueConstraintMappingContext valid() {
		mapping.addMethodCascadeConfig( new MethodConstraintLocation( method ) );
		return this;
	}

	/**
	 * Changes the parameter for which added constraints apply.
	 *
	 * @param index The parameter index.
	 *
	 * @return Returns a new {@code ConstraintsForTypeMethodElement} instance allowing method chaining.
	 */
	public ParameterConstraintMappingContext parameter(int index) {
		return new ParameterConstraintMappingContextImpl(
				beanClass, method, index, mapping
		);
	}

}
