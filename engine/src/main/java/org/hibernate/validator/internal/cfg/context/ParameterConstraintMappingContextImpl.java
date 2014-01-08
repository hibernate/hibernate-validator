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

import javax.validation.ParameterNameProvider;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.CrossParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ExecutableConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;

/**
 * Constraint mapping creational context which allows to configure the constraints for one method parameter.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class ParameterConstraintMappingContextImpl
		extends CascadableConstraintMappingContextImplBase<ParameterConstraintMappingContext>
		implements ParameterConstraintMappingContext {

	private final ExecutableConstraintMappingContextImpl executableContext;
	private final int parameterIndex;

	public ParameterConstraintMappingContextImpl(ExecutableConstraintMappingContextImpl executableContext, int parameterIndex) {
		super( executableContext.getTypeContext().getConstraintMapping() );

		this.executableContext = executableContext;
		this.parameterIndex = parameterIndex;
	}

	@Override
	protected ParameterConstraintMappingContext getThis() {
		return this;
	}

	@Override
	public ParameterConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		super.addConstraint(
				ConfiguredConstraint.forParameter(
						definition,
						executableContext.getExecutable(),
						parameterIndex
				)
		);
		return this;
	}

	@Override
	public ParameterConstraintMappingContext parameter(int index) {
		return executableContext.parameter( index );
	}

	@Override
	public CrossParameterConstraintMappingContext crossParameter() {
		return executableContext.crossParameter();
	}

	@Override
	public ReturnValueConstraintMappingContext returnValue() {
		return executableContext.returnValue();
	}

	@Override
	public ConstructorConstraintMappingContext constructor(Class<?>... parameterTypes) {
		return executableContext.getTypeContext().constructor( parameterTypes );
	}

	@Override
	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		return executableContext.getTypeContext().method( name, parameterTypes );
	}

	public ConstrainedParameter build(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider) {
		return new ConstrainedParameter(
				ConfigurationSource.API,
				new ExecutableConstraintLocation( executableContext.getExecutable(), parameterIndex ),
				executableContext.getExecutable().getParameterNames( parameterNameProvider ).get( parameterIndex ),
				getConstraints( constraintHelper ),
				groupConversions,
				isCascading,
				false
		);
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
