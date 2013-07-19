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

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;

/**
 * Constraint mapping creational context which allows to configure the constraints for one method return value.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class ReturnValueConstraintMappingContextImpl
		extends ConstraintMappingContextImplBase
		implements ReturnValueConstraintMappingContext {

	private final MethodConstraintMappingContextImpl methodContext;
	private boolean isCascaded;

	public ReturnValueConstraintMappingContextImpl(MethodConstraintMappingContextImpl methodContext) {
		super( methodContext.getTypeContext().getConstraintMapping() );
		this.methodContext = methodContext;
	}

	@Override
	public ReturnValueConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		super.addConstraint( ConfiguredConstraint.forReturnValue( definition, methodContext.getMethod() ) );
		return this;
	}

	/**
	 * Marks the current property as cascadable.
	 *
	 * @return Returns itself for method chaining.
	 */
	@Override
	public ReturnValueConstraintMappingContext valid() {
		isCascaded = true;
		return this;
	}

	/**
	 * Changes the parameter for which added constraints apply.
	 *
	 * @param index The parameter index.
	 *
	 * @return Returns a new {@code ConstraintsForTypeMethodElement} instance allowing method chaining.
	 */
	@Override
	public ParameterConstraintMappingContext parameter(int index) {
		return methodContext.parameter( index );
	}

	public boolean isCascaded() {
		return isCascaded;
	}

	@Override
	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		return methodContext.getTypeContext().method( name, parameterTypes );
	}
}
