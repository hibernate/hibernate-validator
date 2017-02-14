/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cfg.context;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.cfg.context.CrossParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;
import org.hibernate.validator.internal.engine.cascading.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.cascading.CascadingTypeParameter;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A constraint mapping creational context which allows to select the parameter or
 * return value to which the next operations shall apply.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 */
abstract class ExecutableConstraintMappingContextImpl {

	private static final Log log = LoggerFactory.make();

	protected final TypeConstraintMappingContextImpl<?> typeContext;
	protected final Executable executable;
	private final ParameterConstraintMappingContextImpl[] parameterContexts;
	private ReturnValueConstraintMappingContextImpl returnValueContext;
	private CrossParameterConstraintMappingContextImpl crossParameterContext;

	protected ExecutableConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Executable executable) {
		this.typeContext = typeContext;
		this.executable = executable;
		this.parameterContexts = new ParameterConstraintMappingContextImpl[executable.getParameterTypes().length];
	}

	public ParameterConstraintMappingContext parameter(int index) {
		if ( index < 0 || index >= executable.getParameterTypes().length ) {
			throw log.getInvalidExecutableParameterIndexException( executable, index );
		}

		ParameterConstraintMappingContextImpl context = parameterContexts[index];

		if ( context != null ) {
			throw log.getParameterHasAlreadyBeConfiguredViaProgrammaticApiException(
					typeContext.getBeanClass(),
					executable,
					index
			);
		}

		context = new ParameterConstraintMappingContextImpl( this, index );
		parameterContexts[index] = context;
		return context;
	}

	public CrossParameterConstraintMappingContext crossParameter() {
		if ( crossParameterContext != null ) {
			throw log.getCrossParameterElementHasAlreadyBeConfiguredViaProgrammaticApiException(
					typeContext.getBeanClass(),
					executable
			);
		}

		crossParameterContext = new CrossParameterConstraintMappingContextImpl( this );
		return crossParameterContext;
	}

	public ReturnValueConstraintMappingContext returnValue() {
		if ( returnValueContext != null ) {
			throw log.getReturnValueHasAlreadyBeConfiguredViaProgrammaticApiException(
					typeContext.getBeanClass(),
					executable
			);
		}

		returnValueContext = new ReturnValueConstraintMappingContextImpl( this );
		return returnValueContext;
	}

	public Executable getExecutable() {
		return executable;
	}

	public TypeConstraintMappingContextImpl<?> getTypeContext() {
		return typeContext;
	}

	public ConstrainedElement build(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ExecutableParameterNameProvider parameterNameProvider, ValueExtractorManager valueExtractorManager) {
		// TODO HV-919 Support specification of type parameter constraints via XML and API
		return new ConstrainedExecutable(
				ConfigurationSource.API,
				executable,
				getParameters( constraintHelper, typeResolutionHelper, parameterNameProvider, valueExtractorManager ),
				crossParameterContext != null ? crossParameterContext.getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ) : Collections.<MetaConstraint<?>>emptySet(),
				returnValueContext != null ? returnValueContext.getConstraints( constraintHelper, typeResolutionHelper, valueExtractorManager ) : Collections.<MetaConstraint<?>>emptySet(),
				Collections.emptySet(),
				returnValueContext != null ? returnValueContext.getGroupConversions() : Collections.<Class<?>, Class<?>>emptyMap(),
				getCascadedTypeParameters( executable, returnValueContext != null && returnValueContext.isCascading() )
		);
	}

	private List<ConstrainedParameter> getParameters(ConstraintHelper constraintHelper, TypeResolutionHelper typeResolutionHelper,
			ExecutableParameterNameProvider parameterNameProvider, ValueExtractorManager valueExtractorManager) {
		List<ConstrainedParameter> constrainedParameters = newArrayList();
		List<String> parameterNames = parameterNameProvider.getParameterNames( executable );

		for ( int i = 0; i < parameterContexts.length; i++ ) {
			ParameterConstraintMappingContextImpl parameter = parameterContexts[i];
			if ( parameter != null ) {
				constrainedParameters.add( parameter.build( constraintHelper, typeResolutionHelper, parameterNameProvider, valueExtractorManager ) );
			}
			else {
				constrainedParameters.add(
						new ConstrainedParameter(
								ConfigurationSource.API,
								executable,
								ReflectionHelper.typeOf( executable, i ),
								i,
								parameterNames.get( i )
						)
				);
			}
		}

		return constrainedParameters;
	}

	private List<CascadingTypeParameter> getCascadedTypeParameters(Executable executable, boolean isCascaded) {
		if ( isCascaded ) {
			boolean isArray = executable instanceof Method && ( (Method) executable ).getReturnType().isArray();
			return Collections.singletonList( isArray
					? CascadingTypeParameter.arrayElement( ReflectionHelper.typeOf( executable ) )
					: CascadingTypeParameter.annotatedObject( ReflectionHelper.typeOf( executable ) ) );
		}
		else {
			return Collections.emptyList();
		}
	}
}
