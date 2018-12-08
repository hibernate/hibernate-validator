/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.reflect.Type;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.ContainerElementConstraintMappingContext;
import org.hibernate.validator.cfg.context.CrossParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;

/**
 * Constraint mapping creational context which allows to configure the constraints for one method parameter.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 */
final class ParameterConstraintMappingContextImpl
		extends CascadableConstraintMappingContextImplBase<ParameterConstraintMappingContext>
		implements ParameterConstraintMappingContext {

	private final ExecutableConstraintMappingContextImpl executableContext;
	private final int parameterIndex;

	ParameterConstraintMappingContextImpl(ExecutableConstraintMappingContextImpl executableContext, int parameterIndex) {
		super(
			executableContext.getTypeContext().getConstraintMapping(),
			executableContext.callable.getParameterGenericType( parameterIndex )
		);

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
						executableContext.getCallable(),
						parameterIndex
				)
		);
		return this;
	}

	@Override
	public ParameterConstraintMappingContext ignoreAnnotations(boolean ignoreAnnotations) {
		mapping.getAnnotationProcessingOptions().ignoreConstraintAnnotationsOnParameter(
				executableContext.getCallable(),
				parameterIndex,
				ignoreAnnotations
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

	@Override
	public ContainerElementConstraintMappingContext containerElementType() {
		return super.containerElement(
				this,
				executableContext.getTypeContext(),
				ConstraintLocation.forParameter( executableContext.getCallable(), parameterIndex )
		);
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType(int index, int... nestedIndexes) {
		return super.containerElement(
				this,
				executableContext.getTypeContext(),
				ConstraintLocation.forParameter( executableContext.getCallable(), parameterIndex ),
				index,
				nestedIndexes
		);
	}

	public ConstrainedParameter build(ConstraintCreationContext constraintCreationContext) {
		Type parameterType = executableContext.getCallable().getParameterGenericType( parameterIndex );

		return new ConstrainedParameter(
				ConfigurationSource.API,
				executableContext.getCallable(),
				parameterType,
				parameterIndex,
				getConstraints( constraintCreationContext ),
				getTypeArgumentConstraints( constraintCreationContext ),
				getCascadingMetaDataBuilder()
		);
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
