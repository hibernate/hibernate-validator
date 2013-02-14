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
package org.hibernate.validator.internal.xml;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ParameterNameProvider;
import javax.validation.ValidationException;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.CrossParameterConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ExecutableConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Builder for constraint methods and constructors.
 *
 * @author Hardy Ferentschik
 */
public class ConstraintExecutableBuilder {
	private static final Log log = LoggerFactory.make();

	private ConstraintExecutableBuilder() {
	}

	public static Set<ConstrainedExecutable> buildMethodConstrainedExecutable(List<MethodType> methods,
																			  Class<?> beanClass,
																			  String defaultPackage,
																			  ParameterNameProvider parameterNameProvider,
																			  ConstraintHelper constraintHelper,
																			  AnnotationProcessingOptions annotationProcessingOptions) {
		Set<ConstrainedExecutable> constrainedExecutables = newHashSet();
		for ( MethodType methodType : methods ) {
			// parse the parameters
			List<Class<?>> parameterTypes = createParameterTypes(
					methodType.getParameter(),
					beanClass,
					defaultPackage
			);

			String methodName = methodType.getName();

			final Method method = ReflectionHelper.getDeclaredMethod(
					beanClass,
					methodName,
					parameterTypes.toArray( new Class[parameterTypes.size()] )
			);

			if ( method == null ) {
				throw log.getBeanDoesNotContainMethodException(
						beanClass.getName(),
						methodName,
						parameterTypes
				);
			}

			ExecutableElement constructorExecutableElement = ExecutableElement.forMethod( method );

			// ignore annotations
			boolean ignoreConstructorAnnotations = methodType.getIgnoreAnnotations() == null ? false : methodType
					.getIgnoreAnnotations();
			if ( ignoreConstructorAnnotations ) {
				annotationProcessingOptions.ignoreConstraintAnnotationsOnMethod( method );
			}

			ConstrainedExecutable constrainedExecutable = parseExecutableType(
					defaultPackage,
					methodType.getParameter(),
					methodType.getCrossParameterConstraint(),
					methodType.getReturnValue(),
					constructorExecutableElement,
					constraintHelper,
					parameterNameProvider
			);

			constrainedExecutables.add( constrainedExecutable );
		}
		return constrainedExecutables;
	}

	public static Set<ConstrainedExecutable> buildConstructorConstrainedExecutable(List<ConstructorType> constructors,
																				   Class<?> beanClass,
																				   String defaultPackage,
																				   ParameterNameProvider parameterNameProvider,
																				   ConstraintHelper constraintHelper,
																				   AnnotationProcessingOptions annotationProcessingOptions) {
		Set<ConstrainedExecutable> constrainedExecutables = newHashSet();
		for ( ConstructorType constructorType : constructors ) {
			// parse the parameters
			List<Class<?>> constructorParameterTypes = createParameterTypes(
					constructorType.getParameter(),
					beanClass,
					defaultPackage
			);

			final Constructor constructor = ReflectionHelper.getConstructor(
					beanClass,
					constructorParameterTypes.toArray( new Class[constructorParameterTypes.size()] )
			);
			if ( constructor == null ) {
				throw log.getBeanDoesNotContainConstructorException( beanClass.getName(), constructorParameterTypes );
			}

			ExecutableElement constructorExecutableElement = ExecutableElement.forConstructor( constructor );

			// ignore annotations
			boolean ignoreConstructorAnnotations = constructorType.getIgnoreAnnotations() == null ? false : constructorType
					.getIgnoreAnnotations();
			if ( ignoreConstructorAnnotations ) {
				annotationProcessingOptions.ignoreConstraintAnnotationsOnConstructor( constructor );
			}

			ConstrainedExecutable constrainedExecutable = parseExecutableType(
					defaultPackage,
					constructorType.getParameter(),
					constructorType.getCrossParameterConstraint(),
					constructorType.getReturnValue(),
					constructorExecutableElement,
					constraintHelper,
					parameterNameProvider

			);
			constrainedExecutables.add( constrainedExecutable );
		}
		return constrainedExecutables;
	}

	private static ConstrainedExecutable parseExecutableType(String defaultPackage,
															 List<ParameterType> parameterTypeList,
															 List<ConstraintType> crossParameterConstraintList,
															 ReturnValueType returnValueType,
															 ExecutableElement executableElement,
															 ConstraintHelper constraintHelper,
															 ParameterNameProvider parameterNameProvider) {
		List<ConstrainedParameter> parameterMetaData = ConstraintParameterBuilder.buildConstrainedParameters(
				parameterTypeList,
				executableElement,
				defaultPackage,
				constraintHelper,
				parameterNameProvider
		);

		Set<MetaConstraint<?>> crossParameterConstraints = newHashSet();
		CrossParameterConstraintLocation constraintLocation = new CrossParameterConstraintLocation(
				executableElement
		);
		for ( ConstraintType constraintType : crossParameterConstraintList ) {
			ConstraintDescriptorImpl<?> constraintDescriptor = ConstraintDescriptorBuilder.buildConstraintDescriptor(
					constraintType,
					executableElement.getElementType(),
					defaultPackage,
					constraintHelper
			);
			@SuppressWarnings("unchecked")
			MetaConstraint<?> metaConstraint = new MetaConstraint( constraintDescriptor, constraintLocation );
			crossParameterConstraints.add( metaConstraint );
		}

		// parse the return value
		Set<MetaConstraint<?>> returnValueConstraints = newHashSet();
		Map<Class<?>, Class<?>> groupConversions = newHashMap();
		boolean isCascaded = parseReturnValueType(
				returnValueType,
				executableElement,
				returnValueConstraints,
				groupConversions,
				defaultPackage,
				constraintHelper
		);

		ConstrainedExecutable constrainedExecutable = new ConstrainedExecutable(
				ConfigurationSource.XML,
				new ExecutableConstraintLocation( executableElement ),
				parameterMetaData,
				crossParameterConstraints,
				returnValueConstraints,
				groupConversions,
				isCascaded
		);

		return constrainedExecutable;
	}

	private static boolean parseReturnValueType(ReturnValueType returnValueType,
												ExecutableElement executableElement,
												Set<MetaConstraint<?>> returnValueConstraints,
												Map<Class<?>, Class<?>> groupConversions,
												String defaultPackage,
												ConstraintHelper constraintHelper) {
		if ( returnValueType == null ) {
			return false;
		}

		ExecutableConstraintLocation constraintLocation = new ExecutableConstraintLocation(
				executableElement
		);
		for ( ConstraintType constraintType : returnValueType.getConstraint() ) {
			ConstraintDescriptorImpl<?> constraintDescriptor = ConstraintDescriptorBuilder.buildConstraintDescriptor(
					constraintType,
					executableElement.getElementType(),
					defaultPackage,
					constraintHelper
			);
			@SuppressWarnings("unchecked")
			MetaConstraint<?> metaConstraint = new MetaConstraint( constraintDescriptor, constraintLocation );
			returnValueConstraints.add( metaConstraint );
		}
		groupConversions.putAll(
				GroupConversionBuilder.buildGroupConversionMap(
						returnValueType.getConvertGroup(),
						defaultPackage
				)
		);

		return returnValueType.getValid() != null;
	}

	private static List<Class<?>> createParameterTypes(List<ParameterType> parameterList,
													   Class<?> beanClass,
													   String defaultPackage) {
		List<Class<?>> parameterTypes = newArrayList();
		for ( ParameterType parameterType : parameterList ) {
			String type = null;
			try {
				type = parameterType.getType();
				Class<?> parameterClass = ReflectionHelper.loadClass( type, defaultPackage );
				parameterTypes.add( parameterClass );
			}
			catch ( ValidationException e ) {
				throw log.getInvalidParameterTypeException( type, beanClass.getName() );
			}
		}

		return parameterTypes;
	}

}


