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
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

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
			executableContext.executable.getGenericParameterTypes()[parameterIndex]
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
						executableContext.getExecutable(),
						parameterIndex
				)
		);
		return this;
	}

	@Override
	public ParameterConstraintMappingContext ignoreAnnotations(boolean ignoreAnnotations) {
		mapping.getAnnotationProcessingOptions().ignoreConstraintAnnotationsOnParameter(
				executableContext.getExecutable(),
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
				ConstraintLocation.forParameter( executableContext.getExecutable(), parameterIndex )
		);
	}

	@Override
	public ContainerElementConstraintMappingContext containerElementType(int index, int... nestedIndexes) {
		return super.containerElement(
				this,
				executableContext.getTypeContext(),
				ConstraintLocation.forParameter( executableContext.getExecutable(), parameterIndex ),
				index,
				nestedIndexes
		);
	}

	public ConstrainedParameter build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		Type parameterType = ReflectionHelper.typeOf( executableContext.getExecutable(), parameterIndex );

		return new ConstrainedParameter(
				ConfigurationSource.API,
				executableContext.getExecutable(),
				parameterType,
				parameterIndex,
				getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ),
				getTypeArgumentConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ),
				getCascadingMetaDataBuilder()
		);
	}

	@Override
	protected ConstraintType getConstraintType() {
		return ConstraintType.GENERIC;
	}
}
