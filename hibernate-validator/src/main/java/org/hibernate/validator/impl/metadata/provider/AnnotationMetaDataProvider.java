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
package org.hibernate.validator.impl.metadata.provider;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.validation.GroupSequence;
import javax.validation.Valid;

import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.impl.metadata.core.AnnotationIgnores;
import org.hibernate.validator.impl.metadata.core.ConstraintHelper;
import org.hibernate.validator.impl.metadata.core.ConstraintOrigin;
import org.hibernate.validator.impl.metadata.core.MetaConstraint;
import org.hibernate.validator.impl.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.impl.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.impl.metadata.location.MethodConstraintLocation;
import org.hibernate.validator.impl.metadata.raw.BeanConfiguration;
import org.hibernate.validator.impl.metadata.raw.ConfigurationSource;
import org.hibernate.validator.impl.metadata.raw.ConstrainedElement;
import org.hibernate.validator.impl.metadata.raw.ConstrainedField;
import org.hibernate.validator.impl.metadata.raw.ConstrainedMethod;
import org.hibernate.validator.impl.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.impl.metadata.raw.ConstrainedType;
import org.hibernate.validator.impl.util.ReflectionHelper;

import static org.hibernate.validator.impl.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.impl.util.CollectionHelper.newHashSet;

/**
 * @author Gunnar Morling
 */
public class AnnotationMetaDataProvider extends MetaDataProviderImplBase {

	private final AnnotationIgnores annotationIgnores;

	public AnnotationMetaDataProvider(ConstraintHelper constraintHelper, AnnotationIgnores annotationIgnores) {

		super( constraintHelper );

		this.annotationIgnores = annotationIgnores;
	}

	public AnnotationIgnores getAnnotationIgnores() {
		return null;
	}

	public BeanConfiguration<?> getBeanConfiguration(Class<?> beanClass) {

		BeanConfiguration<?> configuration = configuredBeans.get( beanClass );

		if ( configuration != null ) {
			return configuration;
		}

		configuration = retrieveBeanConfiguration( beanClass );
		configuredBeans.put( beanClass, configuration );

		return configuration;
	}

	/**
	 * Retrieves constraint related meta data from the annotations of the given type.
	 *
	 * @return
	 */
	private BeanConfiguration<?> retrieveBeanConfiguration(Class<?> beanClass) {

		Set<ConstrainedElement> propertyMetaData = getPropertyMetaData( beanClass );
		propertyMetaData.addAll( getMethodMetaData( beanClass ) );

		//TODO GM: currently class level constraints are represented by a PropertyMetaData. This
		//works but seems somewhat unnatural
		Set<MetaConstraint<?>> classLevelConstraints = getClassLevelConstraints( beanClass );
		if ( !classLevelConstraints.isEmpty() ) {
			ConstrainedType classLevelMetaData =
					new ConstrainedType(
							ConfigurationSource.ANNOTATION,
							new BeanConstraintLocation( beanClass ),
							classLevelConstraints
					);
			propertyMetaData.add( classLevelMetaData );
		}

		return
				createBeanConfiguration(
						ConfigurationSource.ANNOTATION,
						beanClass,
						propertyMetaData,
						getDefaultGroupSequence( beanClass ),
						getDefaultGroupSequenceProviderClass( beanClass )
				);
	}

	private List<Class<?>> getDefaultGroupSequence(Class<?> beanClass) {

		GroupSequence groupSequenceAnnotation = beanClass.getAnnotation( GroupSequence.class );
		return groupSequenceAnnotation != null ? Arrays.asList( groupSequenceAnnotation.value() ) : null;
	}

	private Class<? extends DefaultGroupSequenceProvider<?>> getDefaultGroupSequenceProviderClass(Class<?> beanClass) {

		GroupSequenceProvider groupSequenceProviderAnnotation = beanClass.getAnnotation( GroupSequenceProvider.class );
		return groupSequenceProviderAnnotation != null ? groupSequenceProviderAnnotation.value() : null;
	}

	private Set<MetaConstraint<?>> getClassLevelConstraints(Class<?> clazz) {
		if ( annotationIgnores.isIgnoreAnnotations( clazz ) ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> classLevelConstraints = newHashSet();

		// HV-262
		List<ConstraintDescriptorImpl<?>> classMetaData = findClassLevelConstraints( clazz );

		for ( ConstraintDescriptorImpl<?> constraintDescription : classMetaData ) {
			classLevelConstraints.add( createMetaConstraint( clazz, constraintDescription ) );
		}

		return classLevelConstraints;
	}

	private Set<ConstrainedElement> getPropertyMetaData(Class<?> beanClass) {
		Set<ConstrainedElement> propertyMetaData = newHashSet();

		for ( Field field : ReflectionHelper.getDeclaredFields( beanClass ) ) {

			// HV-172
			if ( Modifier.isStatic( field.getModifiers() ) ||
					annotationIgnores.isIgnoreAnnotations( field ) ||
					field.isSynthetic() ) {

				continue;
			}

			propertyMetaData.add( findPropertyMetaData( field ) );
		}
		return propertyMetaData;
	}

	private ConstrainedField findPropertyMetaData(Field field) {

		Set<MetaConstraint<?>> constraints = convertToMetaConstraints(
				findConstraints( field, ElementType.FIELD ),
				field
		);

		boolean isCascading = field.isAnnotationPresent( Valid.class );

		return
				new ConstrainedField(
						ConfigurationSource.ANNOTATION,
						new BeanConstraintLocation( field ),
						constraints,
						isCascading
				);
	}

	private Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, Field field) {
		Set<MetaConstraint<?>> constraints = newHashSet();

		for ( ConstraintDescriptorImpl<?> constraintDescription : constraintDescriptors ) {
			constraints.add( createMetaConstraint( field, constraintDescription ) );
		}
		return constraints;
	}

	private Set<ConstrainedMethod> getMethodMetaData(Class<?> clazz) {

		Set<ConstrainedMethod> methodMetaData = newHashSet();

		final Method[] declaredMethods = ReflectionHelper.getDeclaredMethods( clazz );

		for ( Method method : declaredMethods ) {

			// HV-172; ignoring synthetic methods (inserted by the compiler), as they can't have any constraints
			// anyway and possibly hide the actual method with the same signature in the built meta model
			if ( Modifier.isStatic( method.getModifiers() ) || annotationIgnores.isIgnoreAnnotations( method ) || method
					.isSynthetic() ) {
				continue;
			}

			methodMetaData.add( findMethodMetaData( method ) );
		}

		return methodMetaData;
	}

	/**
	 * Finds all constraint annotations defined for the given method.
	 *
	 * @param method The method to check for constraints annotations.
	 *
	 * @return A meta data object describing the constraints specified for the
	 *         given method.
	 */
	private ConstrainedMethod findMethodMetaData(Method method) {

		List<ConstrainedParameter> parameterConstraints = getParameterMetaData( method );
		boolean isCascading = method.isAnnotationPresent( Valid.class );
		Set<MetaConstraint<?>> constraints =
				convertToMetaConstraints( findConstraints( method, ElementType.METHOD ), method );

		return new ConstrainedMethod(
				ConfigurationSource.ANNOTATION,
				new MethodConstraintLocation( method ),
				parameterConstraints,
				constraints,
				isCascading
		);
	}

	private Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintsDescriptors, Method method) {

		Set<MetaConstraint<?>> constraints = newHashSet();

		for ( ConstraintDescriptorImpl<?> oneDescriptor : constraintsDescriptors ) {
			constraints.add( createReturnValueMetaConstraint( method, oneDescriptor ) );
		}

		return constraints;
	}

	/**
	 * Retrieves constraint related meta data for the parameters of the given
	 * method.
	 *
	 * @param method The method of interest.
	 *
	 * @return A list with parameter meta data for the given method.
	 */
	private List<ConstrainedParameter> getParameterMetaData(Method method) {
		List<ConstrainedParameter> metaData = newArrayList();

		int i = 0;

		for ( Annotation[] annotationsOfOneParameter : method.getParameterAnnotations() ) {

			boolean parameterIsCascading = false;
			String parameterName = DEFAULT_PARAMETER_NAME_PREFIX + i;
			Set<MetaConstraint<?>> constraintsOfOneParameter = newHashSet();

			for ( Annotation oneAnnotation : annotationsOfOneParameter ) {

				//1. collect constraints if this annotation is a constraint annotation
				List<ConstraintDescriptorImpl<?>> constraints = findConstraintAnnotations(
						method.getDeclaringClass(), oneAnnotation, ElementType.PARAMETER
				);
				for ( ConstraintDescriptorImpl<?> constraintDescriptorImpl : constraints ) {
					constraintsOfOneParameter.add(
							createParameterMetaConstraint(
									method, i, constraintDescriptorImpl
							)
					);
				}

				//2. mark parameter as cascading if this annotation is the @Valid annotation
				if ( oneAnnotation.annotationType().equals( Valid.class ) ) {
					parameterIsCascading = true;
				}
			}

			metaData.add(
					new ConstrainedParameter(
							ConfigurationSource.ANNOTATION,
							new MethodConstraintLocation( method, i ),
							parameterName,
							constraintsOfOneParameter,
							parameterIsCascading
					)
			);
			i++;
		}

		return metaData;
	}

	/**
	 * Finds all constraint annotations defined for the given field/method and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param member The fields or method to check for constraints annotations.
	 * @param type The element type the constraint/annotation is placed on.
	 *
	 * @return A list of constraint descriptors for all constraint specified for the given field or method.
	 */
	private List<ConstraintDescriptorImpl<?>> findConstraints(Member member, ElementType type) {
		assert member instanceof Field || member instanceof Method;

		List<ConstraintDescriptorImpl<?>> metaData = new ArrayList<ConstraintDescriptorImpl<?>>();
		for ( Annotation annotation : ( (AnnotatedElement) member ).getAnnotations() ) {
			metaData.addAll( findConstraintAnnotations( member.getDeclaringClass(), annotation, type ) );
		}

		return metaData;
	}

	/**
	 * Finds all constraint annotations defined for the given class and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param beanClass The class to check for constraints annotations.
	 *
	 * @return A list of constraint descriptors for all constraint specified on the given class.
	 */
	private List<ConstraintDescriptorImpl<?>> findClassLevelConstraints(Class<?> beanClass) {
		List<ConstraintDescriptorImpl<?>> metaData = new ArrayList<ConstraintDescriptorImpl<?>>();
		for ( Annotation annotation : beanClass.getAnnotations() ) {
			metaData.addAll( findConstraintAnnotations( beanClass, annotation, ElementType.TYPE ) );
		}
		return metaData;
	}

	/**
	 * Examines the given annotation to see whether it is a single- or multi-valued constraint annotation.
	 *
	 * @param clazz the class we are currently processing
	 * @param annotation The annotation to examine
	 * @param type the element type on which the annotation/constraint is placed on
	 *
	 * @return A list of constraint descriptors or the empty list in case <code>annotation</code> is neither a
	 *         single nor multi-valued annotation.
	 */
	private <A extends Annotation> List<ConstraintDescriptorImpl<?>> findConstraintAnnotations(Class<?> clazz, A annotation, ElementType type) {
		List<ConstraintDescriptorImpl<?>> constraintDescriptors = new ArrayList<ConstraintDescriptorImpl<?>>();

		List<Annotation> constraints = new ArrayList<Annotation>();
		Class<? extends Annotation> annotationType = annotation.annotationType();
		if ( constraintHelper.isConstraintAnnotation( annotationType )
				|| constraintHelper.isBuiltinConstraint( annotationType ) ) {
			constraints.add( annotation );
		}
		else if ( constraintHelper.isMultiValueConstraint( annotationType ) ) {
			constraints.addAll( constraintHelper.getMultiValueConstraints( annotation ) );
		}

		for ( Annotation constraint : constraints ) {
			final ConstraintDescriptorImpl<?> constraintDescriptor = buildConstraintDescriptor(
					clazz, constraint, type
			);
			constraintDescriptors.add( constraintDescriptor );
		}
		return constraintDescriptors;
	}

	private <A extends Annotation> MetaConstraint<?> createMetaConstraint(Class<?> declaringClass, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<A>( descriptor, new BeanConstraintLocation( declaringClass ) );
	}

	private <A extends Annotation> MetaConstraint<?> createMetaConstraint(Member member, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<A>( descriptor, new BeanConstraintLocation( member ) );
	}

	private <A extends Annotation> MetaConstraint<A> createParameterMetaConstraint(Method method, int parameterIndex, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<A>( descriptor, new MethodConstraintLocation( method, parameterIndex ) );
	}

	private <A extends Annotation> MetaConstraint<A> createReturnValueMetaConstraint(Method method, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<A>( descriptor, new MethodConstraintLocation( method ) );
	}

	private <A extends Annotation> ConstraintDescriptorImpl<A> buildConstraintDescriptor(Class<?> clazz, A annotation, ElementType type) {
		return new ConstraintDescriptorImpl<A>( annotation, constraintHelper, type, ConstraintOrigin.DEFINED_LOCALLY );
	}

}
