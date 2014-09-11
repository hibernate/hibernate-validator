/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.metadata.provider;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.ParameterNameProvider;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.IgnoreJavaBaselineVersion;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Extends {@code AnnotationMetaDataProvider} by discovering and registering type use constraints defined on type
 * arguments.
 *
 * @author Khalid Alqinyah
 */
@IgnoreJavaBaselineVersion
public class TypeAnnotationAwareMetaDataProvider extends AnnotationMetaDataProvider {

	private static final Log log = LoggerFactory.make();

	public TypeAnnotationAwareMetaDataProvider(ConstraintHelper constraintHelper,
											   ParameterNameProvider parameterNameProvider,
											   AnnotationProcessingOptions annotationProcessingOptions) {
		super( constraintHelper, parameterNameProvider, annotationProcessingOptions );
	}

	@Override
	protected Set<MetaConstraint<?>> findTypeArgumentsConstraints(Member member) {
		AnnotatedType annotatedType = null;

		if ( member instanceof Field ) {
			annotatedType = ( (Field) member ).getAnnotatedType();
		}

		if ( member instanceof Method ) {
			annotatedType = ( (Method) member ).getAnnotatedReturnType();
		}

		return findTypeArgumentsConstraints( member, annotatedType );
	}

	@Override
	protected Set<MetaConstraint<?>> findTypeArgumentsConstraints(Member member, int i) {
		Parameter parameter = ( (Executable) member ).getParameters()[i];
		try {
			return findTypeArgumentsConstraints( member, parameter.getAnnotatedType() );
		}
		catch ( ArrayIndexOutOfBoundsException ex ) {
			log.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
			return Collections.emptySet();
		}
	}

	private Set<MetaConstraint<?>> findTypeArgumentsConstraints(Member member, AnnotatedType annotatedType) {
		Set<MetaConstraint<?>> typeArgumentsConstraints = newHashSet();
		Optional<AnnotatedType> typeArgument = getAnnotatedActualTypeArguments( annotatedType );
		if ( !typeArgument.isPresent() ) {
			return Collections.emptySet();
		}

		List<ConstraintDescriptorImpl<?>> metaData = findTypeUseConstraints( member, typeArgument.get() );
		Set<MetaConstraint<?>> constraints = convertToTypeArgumentMetaConstraints(
				metaData,
				member,
				typeArgument.get().getType()
		);
		typeArgumentsConstraints.addAll( constraints );
		return typeArgumentsConstraints;
	}

	/**
	 * Finds type use annotation constraints defined on the type argument.
	 */
	private List<ConstraintDescriptorImpl<?>> findTypeUseConstraints(Member member, AnnotatedType typeArgument) {
		List<ConstraintDescriptorImpl<?>> metaData = newArrayList();

		for ( Annotation annotation : typeArgument.getAnnotations() ) {
			metaData.addAll( findConstraintAnnotations( member, annotation, ElementType.TYPE_USE ) );
		}

		return metaData;
	}

	/**
	 * Creates meta constraints for type arguments constraints.
	 */
	private Set<MetaConstraint<?>> convertToTypeArgumentMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, Member member, Type type) {
		Set<MetaConstraint<?>> constraints = newHashSet( constraintDescriptors.size() );
		for ( ConstraintDescriptorImpl<?> constraintDescription : constraintDescriptors ) {
			MetaConstraint<?> metaConstraint = createTypeArgumentMetaConstraint( member, constraintDescription, type );
			constraints.add( metaConstraint );
		}
		return constraints;
	}

	/**
	 * Creates a {@code MetaConstraint} for a type argument constraint.
	 */
	private <A extends Annotation> MetaConstraint<?> createTypeArgumentMetaConstraint(Member member, ConstraintDescriptorImpl<A> descriptor, Type type) {
		return new MetaConstraint<A>( descriptor, ConstraintLocation.forTypeArgument( member, type ) );
	}

	/**
	 * Returns the type argument of a parameterized type. If the type is a {@code Map}, the method returns the value
	 * type argument. If the type has more than one type argument and is not a Map, the method returns an empty {@code
	 * Optional}.
	 */
	private Optional<AnnotatedType> getAnnotatedActualTypeArguments(AnnotatedType annotatedType) {
		if ( annotatedType == null ) {
			return Optional.empty();
		}

		if ( !TypeHelper.isAssignable( AnnotatedParameterizedType.class, annotatedType.getClass() ) ) {
			return Optional.empty();
		}

		AnnotatedType[] annotatedArguments = ( (AnnotatedParameterizedType) annotatedType ).getAnnotatedActualTypeArguments();

		// One type argument, return it
		if ( annotatedArguments.length == 1 ) {
			return Optional.of( annotatedArguments[0] );
		}

		// More than one type argument
		if ( annotatedArguments.length > 1 ) {

			// If it is a Map, return the value type argument
			if ( ReflectionHelper.isMap( annotatedType.getType() ) ) {
				return Optional.of( annotatedArguments[1] );
			}

			// If it is not a Map, log a message and ignore
			log.info( MESSAGES.parameterizedTypesWithMoreThanOneTypeArgument() );
		}

		return Optional.empty();
	}

	@Override
	protected boolean unwrapBasedOnType(Class<?> clazz, boolean hasConstrainedTypeArguments) {
		if ( ReflectionHelper.isIterable( clazz ) ||  ReflectionHelper.isMap( clazz ) ) {
			return false;
		}

		if ( hasConstrainedTypeArguments ) {
			return true;
		}

		return false;
	}
}
