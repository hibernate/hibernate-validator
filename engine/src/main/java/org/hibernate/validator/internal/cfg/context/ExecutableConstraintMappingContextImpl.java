/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.cfg.context;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.validator.cfg.context.CrossParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ParameterConstraintMappingContext;
import org.hibernate.validator.cfg.context.ReturnValueConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A constraint mapping creational context which allows to select the parameter or
 * return value to which the next operations shall apply.
 *
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 */
abstract class ExecutableConstraintMappingContextImpl implements TypeConstraintMappingContextImpl.MemberContext {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	protected final TypeConstraintMappingContextImpl<?> typeContext;
	protected final Executable executable;

	/**
	 * Annotated parameter types rather than generic ones: they preserve the indexes of implicit and synthetic
	 * constructor parameters, just as {@code JavaBeanParameter} does. Cached, as the JDK hands out a defensive copy
	 * on every call.
	 */
	private final AnnotatedType[] annotatedParameterTypes;

	protected Boolean ignoreAnnotations;
	private final ParameterConstraintMappingContextImpl[] parameterContexts;
	private ReturnValueConstraintMappingContextImpl returnValueContext;
	private CrossParameterConstraintMappingContextImpl crossParameterContext;

	protected ExecutableConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Executable executable) {
		this.typeContext = typeContext;
		this.executable = executable;
		this.annotatedParameterTypes = executable.getAnnotatedParameterTypes();
		this.parameterContexts = new ParameterConstraintMappingContextImpl[executable.getParameterCount()];
	}

	public ParameterConstraintMappingContext parameter(int index) {
		if ( index < 0 || index >= executable.getParameterCount() ) {
			throw LOG.getInvalidExecutableParameterIndexException( executableDescription(), index );
		}

		ParameterConstraintMappingContextImpl context = parameterContexts[index];

		if ( context != null ) {
			throw LOG.getParameterHasAlreadyBeConfiguredViaProgrammaticApiException(
					typeContext.getBeanClass(),
					executableDescription(),
					index
			);
		}

		context = new ParameterConstraintMappingContextImpl( this, index );
		parameterContexts[index] = context;
		return context;
	}

	public CrossParameterConstraintMappingContext crossParameter() {
		if ( crossParameterContext != null ) {
			throw LOG.getCrossParameterElementHasAlreadyBeConfiguredViaProgrammaticApiException(
					typeContext.getBeanClass(),
					executableDescription()
			);
		}

		crossParameterContext = new CrossParameterConstraintMappingContextImpl( this );
		return crossParameterContext;
	}

	public ReturnValueConstraintMappingContext returnValue() {
		if ( returnValueContext != null ) {
			throw LOG.getReturnValueHasAlreadyBeConfiguredViaProgrammaticApiException(
					typeContext.getBeanClass(),
					executableDescription()
			);
		}

		returnValueContext = new ReturnValueConstraintMappingContextImpl( this );
		return returnValueContext;
	}

	private String executableDescription() {
		return ExecutableHelper.getExecutableAsString(
				executable.getDeclaringClass().getSimpleName() + "#" + executable.getName(), executable.getParameterTypes() );
	}

	Type getParameterType(int index) {
		return ReflectionHelper.typeOf( annotatedParameterTypes[index] );
	}

	public TypeConstraintMappingContextImpl<?> getTypeContext() {
		return typeContext;
	}

	@Override
	public ResolvedConstraintMapping.ResolvedElement materialize(JavaBeanHelper helper,
			AnnotationProcessingOptionsImpl options,
			Set<Constrainable> resolvedMembers) {
		Callable callable = helper.executable( executable );
		if ( !resolvedMembers.add( callable ) ) {
			if ( executable instanceof Method ) {
				throw LOG.getMethodHasAlreadyBeenConfiguredViaProgrammaticApiException( typeContext.getBeanClass(),
						ExecutableHelper.getExecutableAsString( executable.getName(), executable.getParameterTypes() ) );
			}
			throw LOG.getConstructorHasAlreadyBeConfiguredViaProgrammaticApiException( typeContext.getBeanClass(),
					ExecutableHelper.getExecutableAsString( typeContext.getBeanClass().getSimpleName(), executable.getParameterTypes() ) );
		}
		if ( ignoreAnnotations != null ) {
			options.ignoreConstraintAnnotationsOnMember( callable, ignoreAnnotations );
		}
		List<ResolvedConstraintMapping.ResolvedParameter> parameters = new ArrayList<>();
		for ( int i = 0; i < parameterContexts.length; i++ ) {
			ParameterConstraintMappingContextImpl parameter = parameterContexts[i];
			parameters.add( parameter == null
					? new ResolvedConstraintMapping.ResolvedParameter( callable, i, Set.of(), ResolvedConstraintMapping.ResolvedCascading.empty() )
					: parameter.materialize( callable, options ) );
		}
		if ( crossParameterContext != null && crossParameterContext.ignoreAnnotations != null ) {
			options.ignoreConstraintAnnotationsForCrossParameterConstraint( callable, crossParameterContext.ignoreAnnotations );
		}
		if ( returnValueContext != null && returnValueContext.ignoreAnnotations != null ) {
			options.ignoreConstraintAnnotationsForReturnValue( callable, returnValueContext.ignoreAnnotations );
		}
		ConstraintLocation returnLocation =
				ConstraintLocation.forReturnValue( callable );
		return new ResolvedConstraintMapping.ResolvedExecutable( callable, parameters,
				crossParameterContext == null
						? Set.of()
						: crossParameterContext.materializeConstraints(
								ConstraintLocation.forCrossParameter( callable ) ),
				returnValueContext == null ? Set.of() : returnValueContext.materializeConstraints( returnLocation ),
				returnValueContext == null
						? ResolvedConstraintMapping.ResolvedCascading.empty()
						: returnValueContext.materializeCascading( returnLocation, callable.getType() ) );
	}
}
