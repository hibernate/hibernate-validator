/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.CrossParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;

/**
 * Constraint mapping creational context which allows to configure cross-parameter constraints for a method or constructor.
 *
 * @author Gunnar Morling
 */
final class CrossParameterConstraintMappingContextImpl
		extends ConstraintMappingContextImplBase
		implements CrossParameterConstraintMappingContext {

	private final ExecutableConstraintMappingContextImpl executableContext;

	CrossParameterConstraintMappingContextImpl(ExecutableConstraintMappingContextImpl executableContext) {
		super( executableContext.getTypeContext().getConstraintMapping() );
		this.executableContext = executableContext;
	}

	@Override
	public CrossParameterConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		super.addConstraint( ConfiguredConstraint.forExecutable( definition, executableContext.getExecutable() ) );
		return this;
	}

	@Override
	public ParameterConstraintMappingContext parameter(int index) {
		return executableContext.parameter( index );
	}

	@Override
	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		return executableContext.getTypeContext().method( name, parameterTypes );
	}

	@Override
	public ConstructorConstraintMappingContext constructor(Class<?>... parameterTypes) {
		return executableContext.getTypeContext().constructor( parameterTypes );
	}

	@Override
	public ReturnValueConstraintMappingContext returnValue() {
		return executableContext.returnValue();
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.CROSS_PARAMETER;
	}
}
