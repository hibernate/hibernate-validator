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
package org.hibernate.validator.internal.cfg.context;

import java.lang.reflect.Method;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;
import org.hibernate.validator.internal.metadata.location.MethodConstraintLocation;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Constraint mapping creational context which allows to configure the constraints for one method parameter.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class ParameterConstraintMappingContextImpl
		extends ConstraintMappingContextImplBase
		implements ParameterConstraintMappingContext {

	private static final Log log = LoggerFactory.make();
	
	private final Method method;
	private final int parameterIndex;

	public ParameterConstraintMappingContextImpl(Class<?> beanClass, Method method, int parameterIndex, ConstraintMappingContext mapping) {

		super( beanClass, mapping );

		if ( parameterIndex < 0 || parameterIndex >= method.getParameterTypes().length ) {
			throw log.getInvalidMethodParameterIndexException( method.getName() );
		}

		this.method = method;
		this.parameterIndex = parameterIndex;
	}

	public ParameterConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {

		mapping.addMethodConstraintConfig(
				ConfiguredConstraint.forParameter(
						definition, method, parameterIndex
				)
		);
		return this;
	}

	/**
	 * Marks the currently selected method parameter as cascadable.
	 *
	 * @return Returns itself for method chaining.
	 */
	public ParameterConstraintMappingContext valid() {
		mapping.addMethodCascadeConfig(
				new MethodConstraintLocation(
						method, parameterIndex
				)
		);
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

	/**
	 * Defines constraints on the return value of the current method.
	 *
	 * @return Returns a new {@code ConstraintsForTypeMethodElement} instance allowing method chaining.
	 */
	public ReturnValueConstraintMappingContext returnValue() {
		return new ReturnValueConstraintMappingContextImpl(
				beanClass, method, mapping
		);
	}

}
