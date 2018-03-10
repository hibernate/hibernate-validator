/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
		super.addConstraint( ConfiguredConstraint.forCrossParameter( definition, executableContext.getCallable() ) );
		return this;
	}

	@Override
	public CrossParameterConstraintMappingContext ignoreAnnotations(boolean ignoreAnnotations) {
		mapping.getAnnotationProcessingOptions().ignoreConstraintAnnotationsForCrossParameterConstraint(
				executableContext.getCallable(), ignoreAnnotations
		);
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
