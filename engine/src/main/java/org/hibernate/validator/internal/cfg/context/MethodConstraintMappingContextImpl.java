/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import java.util.Collections;
import java.util.List;
import javax.validation.ParameterNameProvider;

import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.location.ExecutableConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * A constraint mapping creational context which allows to select the parameter or
 * return value to which the next operations shall apply.
 *
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 * @author Gunnar Morling
 */
public class MethodConstraintMappingContextImpl implements MethodConstraintMappingContext {

	private static final Log log = LoggerFactory.make();

	private final TypeConstraintMappingContextImpl<?> typeContext;
	private final Method method;
	private final ParameterConstraintMappingContextImpl[] parameterContexts;
	private ReturnValueConstraintMappingContextImpl returnValueContext;

	public MethodConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Method method) {
		this.typeContext = typeContext;
		this.method = method;
		this.parameterContexts = new ParameterConstraintMappingContextImpl[method.getParameterTypes().length];
	}

	@Override
	public ParameterConstraintMappingContext parameter(int index) {
		if ( index < 0 || index >= method.getParameterTypes().length ) {
			throw log.getInvalidMethodParameterIndexException( method.getName() );
		}

		ParameterConstraintMappingContextImpl context = parameterContexts[index];

		if ( context != null ) {
			throw log.getParameterHasAlreadyBeConfiguredViaProgrammaticApiException(
					typeContext.getBeanClass().getName(),
					StringHelper.getExecutableAsString( method.getName(), method.getParameterTypes() ),
					index
			);
		}

		context = new ParameterConstraintMappingContextImpl( this, index );
		parameterContexts[index] = context;
		return context;
	}

	@Override
	public ReturnValueConstraintMappingContext returnValue() {
		if ( returnValueContext != null ) {
			throw log.getReturnValueHasAlreadyBeConfiguredViaProgrammaticApiException(
					typeContext.getBeanClass().getName(),
					StringHelper.getExecutableAsString( method.getName(), method.getParameterTypes() )
			);
		}

		returnValueContext = new ReturnValueConstraintMappingContextImpl( this );
		return returnValueContext;
	}

	public Method getMethod() {
		return method;
	}

	public TypeConstraintMappingContextImpl<?> getTypeContext() {
		return typeContext;
	}

	public ConstrainedElement build(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider) {
		return new ConstrainedExecutable(
				ConfigurationSource.API,
				new ExecutableConstraintLocation( method ),
				getParameters( constraintHelper, parameterNameProvider ),
				Collections.<MetaConstraint<?>>emptySet(),
				returnValueContext != null ? returnValueContext.getConstraints( constraintHelper ) : Collections.<MetaConstraint<?>>emptySet(),
				Collections.<Class<?>, Class<?>>emptyMap(),
				returnValueContext != null ? returnValueContext.isCascaded() : false
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
								new ExecutableConstraintLocation( method, i ),
								parameterNameProvider.getParameterNames( method ).get( i )
						)
				);
			}
		}

		return constrainedParameters;
	}
}
