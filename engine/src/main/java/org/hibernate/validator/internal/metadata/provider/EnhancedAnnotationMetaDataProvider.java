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
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ParameterNameProvider;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;

import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedTypeArgument;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;

/**
 * @author Khalid Alqinyah
 */
public class EnhancedAnnotationMetaDataProvider extends AnnotationMetaDataProvider {

	public EnhancedAnnotationMetaDataProvider(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider, AnnotationProcessingOptions annotationProcessingOptions) {
		super( constraintHelper, parameterNameProvider, annotationProcessingOptions );
	}

	@Override
	public <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(Class<T> beanClass) {
		// Get the constrained elements from the base AnnotationMetaDataProvider
		List<BeanConfiguration<? super T>> configurations = super.getBeanConfigurationForHierarchy( beanClass );

		// Add constrained type arguments
		for ( BeanConfiguration<? super T> configuration : configurations ) {
			configuration.getConstrainedElements().addAll( getTypeArgumentsMetaData( configuration.getBeanClass() ) );
		}

		return configurations;
	}

	private Set<ConstrainedElement> getTypeArgumentsMetaData(Class<?> beanClass) {
		Set<ConstrainedElement> typeArgumentsMetaData = newHashSet();

		for ( Field field : ReflectionHelper.getDeclaredFields( beanClass ) ) {
			if ( Modifier.isStatic( field.getModifiers() ) ||
					annotationProcessingOptions.areMemberConstraintsIgnoredFor( field ) ||
					field.isSynthetic() ) {

				continue;
			}
			typeArgumentsMetaData.addAll( findTypeArgumentsMetaData( field ) );
		}
		return typeArgumentsMetaData;
	}

	private Set<ConstrainedElement> findTypeArgumentsMetaData(Field field) {
		Set<ConstrainedElement> typeArgumentsMetaData = newHashSet();
		AnnotatedType[] annotatedTypes = getAnnotatedActualTypeArguments( field );

		for ( AnnotatedType annotatedType : annotatedTypes ) {
			ConstrainedTypeArgument constrainedTypeArgument = findConstrainedTypeArgumentsMetaData( field, annotatedType );
			if ( constrainedTypeArgument != null ) {
				typeArgumentsMetaData.add( constrainedTypeArgument );
			}
		}

		return typeArgumentsMetaData;
	}

	private ConstrainedTypeArgument findConstrainedTypeArgumentsMetaData(Field field, AnnotatedType annotatedType) {

		List<ConstraintDescriptorImpl<?>> metaData = findTypeArgumentConstraints( field, annotatedType );

		if ( !metaData.isEmpty() ) {
			boolean isCascading = annotatedType.isAnnotationPresent( Valid.class );
			boolean requiresUnwrapping = annotatedType.isAnnotationPresent( UnwrapValidatedValue.class );

			Set<MetaConstraint<?>> constraints = convertToTypeArgumentMetaConstraints(
					metaData,
					field,
					annotatedType.getType()
			);

			Map<Class<?>, Class<?>> groupConversions = getGroupConversions(
					field.getAnnotation( ConvertGroup.class ),
					field.getAnnotation( ConvertGroup.List.class )
			);

			return new ConstrainedTypeArgument(
					ConfigurationSource.ANNOTATION,
					ConstraintLocation.forTypeArgument( field, annotatedType.getType() ),
					constraints,
					groupConversions,
					isCascading,
					requiresUnwrapping
			);
		}

		return null;
	}

	/**
	 * Finds any constraints specified as type annotation.
	 */
	public List<ConstraintDescriptorImpl<?>> findTypeArgumentConstraints(Field field, AnnotatedType annotatedType) {
		List<ConstraintDescriptorImpl<?>> metaData = newArrayList();

		// Each annotated type might have more than one annotation, process them all
		for ( Annotation annotation : annotatedType.getAnnotations() ) {
			// Check if the annotation is a constraint, and create a meta constraint for it
			metaData.addAll( findConstraintAnnotations( field, annotation, ElementType.TYPE_USE ) );
		}

		return metaData;
	}

	/**
	 * Creates {@code MetaConstraint} for type argument annotations (e.g. {@code List<@Email String>}.
	 */
	private Set<MetaConstraint<?>> convertToTypeArgumentMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, Field field, Type type) {
		Set<MetaConstraint<?>> constraints = newHashSet();
		for ( ConstraintDescriptorImpl<?> constraintDescription : constraintDescriptors ) {
			constraints.add(
					createTypeArgumentMetaConstraint(
							field,
							constraintDescription,
							type
					)
			);
		}
		return constraints;
	}

	/**
	 * When creating a {@code ConstraintLocation} for a type argument, use the actual argument type (e.g. {@code
	 * String}, instead of {@code List<String>}).
	 */
	private <A extends Annotation> MetaConstraint<?> createTypeArgumentMetaConstraint(Member member, ConstraintDescriptorImpl<A> descriptor, Type type) {
		return new MetaConstraint<A>( descriptor, ConstraintLocation.forTypeArgument( member, type ) );
	}

	/**
	 * Returns the actual type arguments for an annotated type.
	 *
	 * @param field containing the the type annotation (e.g. {@code List<@Email String>})
	 *
	 * @return the actual annotated type arguments
	 */
	private static AnnotatedType[] getAnnotatedActualTypeArguments(Field field) {
		if ( field.getAnnotatedType() instanceof AnnotatedParameterizedType ) {
			AnnotatedParameterizedType annotatedParameterizedType = (AnnotatedParameterizedType) field.getAnnotatedType();
			return annotatedParameterizedType.getAnnotatedActualTypeArguments();
		}

		return new AnnotatedType[] { };
	}
}
