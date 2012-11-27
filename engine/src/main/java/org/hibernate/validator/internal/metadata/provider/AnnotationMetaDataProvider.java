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
package org.hibernate.validator.internal.metadata.provider;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AccessibleObject;
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

import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.ConstraintOrigin;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.internal.metadata.location.MethodConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedMethod;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.ConcurrentReferenceHashMap.ReferenceType.SOFT;
import static org.hibernate.validator.internal.util.ReflectionHelper.getMethods;
import static org.hibernate.validator.internal.util.ReflectionHelper.newInstance;

/**
 * {@code MetaDataProvider} which reads the metadata from annotations which is the default configuration source.
 *
 * @author Gunnar Morling
 * @author Hardy Ferentschik
 */
public class AnnotationMetaDataProvider implements MetaDataProvider {
	private static final Log log = LoggerFactory.make();
	/**
	 * The default initial capacity for this cache.
	 */
	static final int DEFAULT_INITIAL_CAPACITY = 16;

	private final ConstraintHelper constraintHelper;
	private final ConcurrentReferenceHashMap<Class<?>, BeanConfiguration<?>> configuredBeans;
	private final AnnotationProcessingOptions annotationProcessingOptions;

	public AnnotationMetaDataProvider(ConstraintHelper constraintHelper, AnnotationProcessingOptions annotationProcessingOptions) {
		this.constraintHelper = constraintHelper;
		this.annotationProcessingOptions = annotationProcessingOptions;
		configuredBeans = new ConcurrentReferenceHashMap<Class<?>, BeanConfiguration<?>>(
				DEFAULT_INITIAL_CAPACITY,
				SOFT,
				SOFT
		);
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return new AnnotationProcessingOptions();
	}

	@Override
	public <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(Class<T> beanClass) {
		List<BeanConfiguration<? super T>> configurations = newArrayList();

		for ( Class<?> hierarchyClass : ReflectionHelper.computeClassHierarchy( beanClass, true ) ) {
			@SuppressWarnings("unchecked")
			BeanConfiguration<? super T> configuration = (BeanConfiguration<? super T>) getBeanConfiguration(
					hierarchyClass
			);
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}

	private BeanConfiguration<?> getBeanConfiguration(Class<?> beanClass) {
		BeanConfiguration<?> configuration = configuredBeans.get( beanClass );

		if ( configuration != null ) {
			return configuration;
		}

		configuration = retrieveBeanConfiguration( beanClass );
		configuredBeans.put( beanClass, configuration );

		return configuration;
	}

	/**
	 * @param beanClass The bean class for which to retrieve the meta data
	 *
	 * @return Retrieves constraint related meta data from the annotations of the given type.
	 */
	private <T> BeanConfiguration<T> retrieveBeanConfiguration(Class<T> beanClass) {
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

		return new BeanConfiguration<T>(
				ConfigurationSource.ANNOTATION,
				beanClass,
				propertyMetaData,
				getDefaultGroupSequence( beanClass ),
				getDefaultGroupSequenceProvider( beanClass )
		);
	}

	private List<Class<?>> getDefaultGroupSequence(Class<?> beanClass) {
		GroupSequence groupSequenceAnnotation = beanClass.getAnnotation( GroupSequence.class );
		return groupSequenceAnnotation != null ? Arrays.asList( groupSequenceAnnotation.value() ) : null;
	}

	private <T> DefaultGroupSequenceProvider<? super T> getDefaultGroupSequenceProvider(Class<T> beanClass) {
		GroupSequenceProvider groupSequenceProviderAnnotation = beanClass.getAnnotation( GroupSequenceProvider.class );

		if ( groupSequenceProviderAnnotation != null ) {
			return newGroupSequenceProviderClassInstance( beanClass, groupSequenceProviderAnnotation.value() );
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private <T> DefaultGroupSequenceProvider<? super T> newGroupSequenceProviderClassInstance(Class<T> beanClass, Class<?> providerClass) {
		Method[] providerMethods = getMethods( providerClass );
		for ( Method method : providerMethods ) {
			Class<?>[] paramTypes = method.getParameterTypes();
			if ( "getValidationGroups".equals( method.getName() ) && !method.isBridge()
					&& paramTypes.length == 1 && paramTypes[0].isAssignableFrom( beanClass ) ) {

				return (DefaultGroupSequenceProvider<? super T>) newInstance(
						providerClass, "the default group sequence provider"
				);
			}
		}

		throw log.getWrongDefaultGroupSequenceProviderTypeException( beanClass.getName() );
	}

	private Set<MetaConstraint<?>> getClassLevelConstraints(Class<?> clazz) {
		if ( annotationProcessingOptions.areClassLevelConstraintAnnotationsIgnored( clazz ) ) {
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
					annotationProcessingOptions.arePropertyLevelConstraintAnnotationsIgnored( field ) ||
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

		return new ConstrainedField(
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
			if ( Modifier.isStatic( method.getModifiers() ) || annotationProcessingOptions.arePropertyLevelConstraintAnnotationsIgnored(
					method
			) || method
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
						oneAnnotation, ElementType.PARAMETER
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
	private List<ConstraintDescriptorImpl<?>> findConstraints(AccessibleObject member, ElementType type) {
		List<ConstraintDescriptorImpl<?>> metaData = new ArrayList<ConstraintDescriptorImpl<?>>();
		for ( Annotation annotation : member.getDeclaredAnnotations() ) {
			metaData.addAll( findConstraintAnnotations( annotation, type ) );
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
		for ( Annotation annotation : beanClass.getDeclaredAnnotations() ) {
			metaData.addAll( findConstraintAnnotations( annotation, ElementType.TYPE ) );
		}
		return metaData;
	}

	/**
	 * Examines the given annotation to see whether it is a single- or multi-valued constraint annotation.
	 *
	 * @param annotation The annotation to examine
	 * @param type the element type on which the annotation/constraint is placed on
	 *
	 * @return A list of constraint descriptors or the empty list in case <code>annotation</code> is neither a
	 *         single nor multi-valued annotation.
	 */
	private <A extends Annotation> List<ConstraintDescriptorImpl<?>> findConstraintAnnotations(A annotation, ElementType type) {
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
					constraint, type
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

	private <A extends Annotation> ConstraintDescriptorImpl<A> buildConstraintDescriptor(A annotation, ElementType type) {
		return new ConstraintDescriptorImpl<A>( annotation, constraintHelper, type, ConstraintOrigin.DEFINED_LOCALLY );
	}
}
