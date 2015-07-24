/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import org.hibernate.validator.cfg.context.ConstructorConstraintMappingContext;
import org.hibernate.validator.cfg.context.CrossParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;
import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import javax.validation.ParameterNameProvider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * A constraint mapping creational context which allows to select the parameter or
 * return value to which the next operations shall apply.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 */
class ExecutableConstraintMappingContextImpl
		implements ConstructorConstraintMappingContext, MethodConstraintMappingContext {

	private static final Log log = LoggerFactory.make();

	private final TypeConstraintMappingContextImpl<?> typeContext;
	private final ExecutableElement executable;
	private final ParameterConstraintMappingContextImpl[] parameterContexts;
	private ReturnValueConstraintMappingContextImpl returnValueContext;
	private CrossParameterConstraintMappingContextImpl crossParameterContext;

	ExecutableConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Constructor<?> constructor) {
		this( typeContext, ExecutableElement.forConstructor( constructor ) );
	}

	ExecutableConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Method method) {
		this( typeContext, ExecutableElement.forMethod( method ) );
	}

	private ExecutableConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, ExecutableElement executable) {
		this.typeContext = typeContext;
		this.executable = executable;
		this.parameterContexts = new ParameterConstraintMappingContextImpl[executable.getParameterTypes().length];
	}

	@Override
	public ParameterConstraintMappingContext parameter(int index) {
		if ( index < 0 || index >= executable.getParameterTypes().length ) {
			throw log.getInvalidExecutableParameterIndexException( executable.getAsString(), index );
		}

		ParameterConstraintMappingContextImpl context = parameterContexts[index];

		if ( context != null ) {
			throw log.getParameterHasAlreadyBeConfiguredViaProgrammaticApiException(
					typeContext.getBeanClass().getName(),
					executable.getAsString(),
					index
			);
		}

		context = new ParameterConstraintMappingContextImpl( this, index );
		parameterContexts[index] = context;
		return context;
	}

	@Override
	public CrossParameterConstraintMappingContext crossParameter() {
		if ( crossParameterContext != null ) {
			throw log.getCrossParameterElementHasAlreadyBeConfiguredViaProgrammaticApiException(
					typeContext.getBeanClass().getName(),
					executable.getAsString()
			);
		}

		crossParameterContext = new CrossParameterConstraintMappingContextImpl( this );
		return crossParameterContext;
	}

	@Override
	public ReturnValueConstraintMappingContext returnValue() {
		if ( returnValueContext != null ) {
			throw log.getReturnValueHasAlreadyBeConfiguredViaProgrammaticApiException(
					typeContext.getBeanClass().getName(),
					executable.getAsString()
			);
		}

		returnValueContext = new ReturnValueConstraintMappingContextImpl( this );
		return returnValueContext;
	}

	public ExecutableElement getExecutable() {
		return executable;
	}

	public TypeConstraintMappingContextImpl<?> getTypeContext() {
		return typeContext;
	}

	public ConstrainedElement build(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider) {
		// TODO HV-919 Support specification of type parameter constraints via XML and API
		return new ConstrainedExecutable(
				ConfigurationSource.API,
				ConstraintLocation.forReturnValue( executable ),
				getParameters( constraintHelper, parameterNameProvider ),
				crossParameterContext != null ? crossParameterContext.getConstraints( constraintHelper ) : Collections.<MetaConstraint<?>>emptySet(),
				returnValueContext != null ? returnValueContext.getConstraints( constraintHelper ) : Collections.<MetaConstraint<?>>emptySet(),
				Collections.<MetaConstraint<?>>emptySet(),
				returnValueContext != null ? returnValueContext.getGroupConversions() : Collections.<Class<?>, Class<?>>emptyMap(),
				returnValueContext != null ? returnValueContext.isCascading() : false,
				returnValueContext != null ? returnValueContext.unwrapMode() : UnwrapMode.AUTOMATIC
		);
	}

	private List<ConstrainedParameter> getParameters(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider) {
		List<ConstrainedParameter> constrainedParameters = newArrayList();

		for ( int i = 0; i < parameterContexts.length; i++ ) {
			ParameterConstraintMappingContextImpl parameter = parameterContexts[i];
			if ( parameter != null ) {
				constrainedParameters.add( parameter.build( constraintHelper, parameterNameProvider ) );
			}
			else {
				constrainedParameters.add(
						new ConstrainedParameter(
								ConfigurationSource.API,
								ConstraintLocation.forParameter( executable, i ),
								ReflectionHelper.typeOf( executable, i ),
								i,
								executable.getParameterNames( parameterNameProvider ).get( i )
						)
				);
			}
		}

		return constrainedParameters;
	}
}
