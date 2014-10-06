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
import java.lang.reflect.AccessibleObject;
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
import javax.validation.Valid;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.util.IgnoreJava6Requirement;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Extends {@code AnnotationMetaDataProvider} by discovering and registering constraints defined via Java 8 type
 * annotations.
 *
 * @author Khalid Alqinyah
 * @author Hardy Ferentschik
 */
@IgnoreJava6Requirement
public class TypeAnnotationAwareMetaDataProvider extends AnnotationMetaDataProvider {

	private static final Log log = LoggerFactory.make();

	public TypeAnnotationAwareMetaDataProvider(ConstraintHelper constraintHelper,
											   ParameterNameProvider parameterNameProvider,
											   AnnotationProcessingOptions annotationProcessingOptions) {
		super( constraintHelper, parameterNameProvider, annotationProcessingOptions );
	}

	@Override
	protected Set<MetaConstraint<?>> findTypeAnnotationConstraintsForMember(Member member) {
		AnnotatedType annotatedType = null;

		if ( member instanceof Field ) {
			annotatedType = ( (Field) member ).getAnnotatedType();
		}

		if ( member instanceof Method ) {
			annotatedType = ( (Method) member ).getAnnotatedReturnType();
		}

		return findTypeArgumentsConstraints(
				member,
				annotatedType,
				( (AccessibleObject) member ).isAnnotationPresent( Valid.class )
		);
	}

	@Override
	protected Set<MetaConstraint<?>> findTypeAnnotationConstraintsForExecutableParameter(Member member, int i) {
		Parameter parameter = ( (Executable) member ).getParameters()[i];
		try {
			return findTypeArgumentsConstraints(
					member,
					parameter.getAnnotatedType(),
					parameter.isAnnotationPresent( Valid.class )
			);
		}
		catch ( ArrayIndexOutOfBoundsException ex ) {
			log.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
			return Collections.emptySet();
		}
	}

	private Set<MetaConstraint<?>> findTypeArgumentsConstraints(Member member, AnnotatedType annotatedType, boolean isCascaded) {
		Optional<AnnotatedType> typeParameter = getTypeParameter( annotatedType );
		if ( !typeParameter.isPresent() ) {
			return Collections.emptySet();
		}

		List<ConstraintDescriptorImpl<?>> constraintDescriptors = findTypeUseConstraints( member, typeParameter.get() );
		if ( constraintDescriptors.isEmpty() ) {
			return Collections.emptySet();
		}

		// HV-925
		// We need to determine the validated type used for constraint validator resolution.
		// Iterables and maps need special treatment at this point, since the validated type is the type of the
		// specified type parameter. In the other cases the validated type is the parameterized type, eg Optional<String>.
		// In the latter case a value unwrapping has to occur
		Type validatedType = annotatedType.getType();
		if ( ReflectionHelper.isIterable( annotatedType.getType() ) || ReflectionHelper.isMap( annotatedType.getType() ) ) {
			if ( !isCascaded ) {
				throw log.getTypeAnnotationConstraintOnIterableRequiresUseOfValidAnnotationException(
						member.getDeclaringClass().getName(),
						member.getName()
				);
			}
			validatedType = typeParameter.get().getType();
		}

		return convertToTypeArgumentMetaConstraints(
				constraintDescriptors,
				member,
				validatedType
		);
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
	private Optional<AnnotatedType> getTypeParameter(AnnotatedType annotatedType) {
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
			log.parameterizedTypeWithMoreThanOneTypeArgumentIsNotSupported();
		}

		return Optional.empty();
	}
}
