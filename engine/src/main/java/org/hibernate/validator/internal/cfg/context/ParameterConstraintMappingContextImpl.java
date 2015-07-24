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
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.ReflectionHelper;

import javax.validation.ParameterNameProvider;
import java.util.Collections;

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
		// TODO HV-919 Support specification of type parameter constraints via XML and API
		return new ConstrainedParameter(
				ConfigurationSource.API,
				ConstraintLocation.forParameter( executableContext.getExecutable(), parameterIndex ),
				ReflectionHelper.typeOf( executableContext.getExecutable(), parameterIndex ),
				parameterIndex,
				executableContext.getExecutable().getParameterNames( parameterNameProvider ).get( parameterIndex ),
				getConstraints( constraintHelper ),
				Collections.<MetaConstraint<?>>emptySet(),
				groupConversions,
				isCascading,
				unwrapMode()
		);
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
