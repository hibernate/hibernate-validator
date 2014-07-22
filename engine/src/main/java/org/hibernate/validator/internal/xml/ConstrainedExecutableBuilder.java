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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ParameterNameProvider;
import javax.validation.ValidationException;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructor;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * Builder for constrained methods and constructors.
 *
 * @author Hardy Ferentschik
 */
class ConstrainedExecutableBuilder {

	private static final Log log = LoggerFactory.make();

	private ConstrainedExecutableBuilder() {
	}

	static Set<ConstrainedExecutable> buildMethodConstrainedExecutable(List<MethodType> methods,
																			  Class<?> beanClass,
																			  String defaultPackage,
																			  ParameterNameProvider parameterNameProvider,
																			  ConstraintHelper constraintHelper,
																			  AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		Set<ConstrainedExecutable> constrainedExecutables = newHashSet();
		List<Method> alreadyProcessedMethods = newArrayList();
		for ( MethodType methodType : methods ) {
			// parse the parameters
			List<Class<?>> parameterTypes = createParameterTypes(
					methodType.getParameter(),
					beanClass,
					defaultPackage
			);

			String methodName = methodType.getName();

			final Method method = run(
					GetDeclaredMethod.action(
							beanClass,
							methodName,
							parameterTypes.toArray( new Class[parameterTypes.size()] )
					)
			);

			if ( method == null ) {
				throw log.getBeanDoesNotContainMethodException(
						beanClass.getName(),
						methodName,
						parameterTypes
				);
			}

			if ( alreadyProcessedMethods.contains( method ) ) {
				throw log.getMethodIsDefinedTwiceInMappingXmlForBeanException( method.toString(), beanClass.getName() );
			}
			else {
				alreadyProcessedMethods.add( method );
			}

			ExecutableElement methodExecutableElement = ExecutableElement.forMethod( method );

			// ignore annotations
			if ( methodType.getIgnoreAnnotations() != null ) {
				annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
						method,
						methodType.getIgnoreAnnotations()
				);
			}

			ConstrainedExecutable constrainedExecutable = parseExecutableType(
					defaultPackage,
					methodType.getParameter(),
					methodType.getCrossParameter(),
					methodType.getReturnValue(),
					methodExecutableElement,
					constraintHelper,
					parameterNameProvider,
					annotationProcessingOptions
			);

			constrainedExecutables.add( constrainedExecutable );
		}
		return constrainedExecutables;
	}

	static Set<ConstrainedExecutable> buildConstructorConstrainedExecutable(List<ConstructorType> constructors,
																				   Class<?> beanClass,
																				   String defaultPackage,
																				   ParameterNameProvider parameterNameProvider,
																				   ConstraintHelper constraintHelper,
																				   AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		Set<ConstrainedExecutable> constrainedExecutables = newHashSet();
		List<Constructor<?>> alreadyProcessedConstructors = newArrayList();
		for ( ConstructorType constructorType : constructors ) {
			// parse the parameters
			List<Class<?>> constructorParameterTypes = createParameterTypes(
					constructorType.getParameter(),
					beanClass,
					defaultPackage
			);

			final Constructor<?> constructor = run(
					GetDeclaredConstructor.action(
							beanClass,
							constructorParameterTypes.toArray( new Class[constructorParameterTypes.size()] )
					)
			);

			if ( constructor == null ) {
				throw log.getBeanDoesNotContainConstructorException(
						beanClass.getName(),
						StringHelper.join( constructorParameterTypes, ", " )
				);
			}
			if ( alreadyProcessedConstructors.contains( constructor ) ) {
				throw log.getConstructorIsDefinedTwiceInMappingXmlForBeanException(
						constructor.toString(),
						beanClass.getName()
				);
			}
			else {
				alreadyProcessedConstructors.add( constructor );
			}

			ExecutableElement constructorExecutableElement = ExecutableElement.forConstructor( constructor );

			// ignore annotations
			if ( constructorType.getIgnoreAnnotations() != null ) {
				annotationProcessingOptions.ignoreConstraintAnnotationsOnMember(
						constructor,
						constructorType.getIgnoreAnnotations()
				);
			}

			ConstrainedExecutable constrainedExecutable = parseExecutableType(
					defaultPackage,
					constructorType.getParameter(),
					constructorType.getCrossParameter(),
					constructorType.getReturnValue(),
					constructorExecutableElement,
					constraintHelper,
					parameterNameProvider,
					annotationProcessingOptions
			);
			constrainedExecutables.add( constrainedExecutable );
		}
		return constrainedExecutables;
	}

	private static ConstrainedExecutable parseExecutableType(String defaultPackage,
															 List<ParameterType> parameterTypeList,
															 CrossParameterType crossParameterType,
															 ReturnValueType returnValueType,
															 ExecutableElement executableElement,
															 ConstraintHelper constraintHelper,
															 ParameterNameProvider parameterNameProvider,
															 AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		List<ConstrainedParameter> parameterMetaData = ConstrainedParameterBuilder.buildConstrainedParameters(
				parameterTypeList,
				executableElement,
				defaultPackage,
				constraintHelper,
				parameterNameProvider,
				annotationProcessingOptions
		);

		Set<MetaConstraint<?>> crossParameterConstraints = parseCrossParameterConstraints(
				defaultPackage,
				crossParameterType,
				executableElement,
				constraintHelper,
				annotationProcessingOptions
		);

		// parse the return value
		Set<MetaConstraint<?>> returnValueConstraints = newHashSet();
		Map<Class<?>, Class<?>> groupConversions = newHashMap();
		boolean isCascaded = parseReturnValueType(
				returnValueType,
				executableElement,
				returnValueConstraints,
				groupConversions,
				defaultPackage,
				constraintHelper,
				annotationProcessingOptions
		);

		return new ConstrainedExecutable(
				ConfigurationSource.XML,
				ConstraintLocation.forReturnValue( executableElement ),
				parameterMetaData,
				crossParameterConstraints,
				returnValueConstraints,
				groupConversions,
				isCascaded,
				false
		);
	}

	private static Set<MetaConstraint<?>> parseCrossParameterConstraints(String defaultPackage,
																		 CrossParameterType crossParameterType,
																		 ExecutableElement executableElement,
																		 ConstraintHelper constraintHelper,
																		 AnnotationProcessingOptionsImpl annotationProcessingOptions) {

		Set<MetaConstraint<?>> crossParameterConstraints = newHashSet();
		if ( crossParameterType == null ) {
			return crossParameterConstraints;
		}

		ConstraintLocation constraintLocation = ConstraintLocation.forCrossParameter( executableElement );

		for ( ConstraintType constraintType : crossParameterType.getConstraint() ) {
			MetaConstraint<?> metaConstraint = MetaConstraintBuilder.buildMetaConstraint(
					constraintLocation,
					constraintType,
					executableElement.getElementType(),
					defaultPackage,
					constraintHelper,
					ConstraintDescriptorImpl.ConstraintType.CROSS_PARAMETER
			);
			crossParameterConstraints.add( metaConstraint );
		}

		// ignore annotations
		if ( crossParameterType.getIgnoreAnnotations() != null ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsForCrossParameterConstraint(
					executableElement.getMember(),
					crossParameterType.getIgnoreAnnotations()
			);
		}

		return crossParameterConstraints;
	}

	private static boolean parseReturnValueType(ReturnValueType returnValueType,
												ExecutableElement executableElement,
												Set<MetaConstraint<?>> returnValueConstraints,
												Map<Class<?>, Class<?>> groupConversions,
												String defaultPackage,
												ConstraintHelper constraintHelper,
												AnnotationProcessingOptionsImpl annotationProcessingOptions) {
		if ( returnValueType == null ) {
			return false;
		}

		ConstraintLocation constraintLocation = ConstraintLocation.forReturnValue( executableElement );
		for ( ConstraintType constraint : returnValueType.getConstraint() ) {
			MetaConstraint<?> metaConstraint = MetaConstraintBuilder.buildMetaConstraint(
					constraintLocation,
					constraint,
					executableElement.getElementType(),
					defaultPackage,
					constraintHelper,
					ConstraintDescriptorImpl.ConstraintType.GENERIC
			);
			returnValueConstraints.add( metaConstraint );
		}
		groupConversions.putAll(
				GroupConversionBuilder.buildGroupConversionMap(
						returnValueType.getConvertGroup(),
						defaultPackage
				)
		);

		// ignore annotations
		if ( returnValueType.getIgnoreAnnotations() != null ) {
			annotationProcessingOptions.ignoreConstraintAnnotationsForReturnValue(
					executableElement.getMember(),
					returnValueType.getIgnoreAnnotations()
			);
		}

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
				Class<?> parameterClass = ClassLoadingHelper.loadClass( type, defaultPackage );
				parameterTypes.add( parameterClass );
			}
			catch ( ValidationException e ) {
				throw log.getInvalidParameterTypeException( type, beanClass.getName() );
			}
		}

		return parameterTypes;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
