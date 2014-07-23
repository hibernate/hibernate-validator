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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.GroupSequence;
import javax.validation.ParameterNameProvider;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;

import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.metadata.raw.ExecutableElement;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.CollectionHelper.Partitioner;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.classhierarchy.ClassHierarchyHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructors;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredFields;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;
import org.hibernate.validator.internal.util.privilegedactions.GetMethods;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.CollectionHelper.partition;
import static org.hibernate.validator.internal.util.ConcurrentReferenceHashMap.ReferenceType.SOFT;

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
	private final ParameterNameProvider parameterNameProvider;

	public AnnotationMetaDataProvider(ConstraintHelper constraintHelper,
			ParameterNameProvider parameterNameProvider,
			AnnotationProcessingOptions annotationProcessingOptions) {
		this.constraintHelper = constraintHelper;
		this.parameterNameProvider = parameterNameProvider;
		this.annotationProcessingOptions = annotationProcessingOptions;
		configuredBeans = new ConcurrentReferenceHashMap<Class<?>, BeanConfiguration<?>>(
				DEFAULT_INITIAL_CAPACITY,
				SOFT,
				SOFT
		);
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return new AnnotationProcessingOptionsImpl();
	}

	@Override
	public <T> List<BeanConfiguration<? super T>> getBeanConfigurationForHierarchy(Class<T> beanClass) {
		List<BeanConfiguration<? super T>> configurations = newArrayList();

		for ( Class<? super T> hierarchyClass : ClassHierarchyHelper.getHierarchy( beanClass ) ) {
			BeanConfiguration<? super T> configuration = getBeanConfiguration( hierarchyClass );
			if ( configuration != null ) {
				configurations.add( configuration );
			}
		}

		return configurations;
	}

	private <T> BeanConfiguration<T> getBeanConfiguration(Class<T> beanClass) {
		@SuppressWarnings("unchecked")
		BeanConfiguration<T> configuration = (BeanConfiguration<T>) configuredBeans.get( beanClass );

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
		Set<ConstrainedElement> constrainedElements = getFieldMetaData( beanClass );
		constrainedElements.addAll( getMethodMetaData( beanClass ) );
		constrainedElements.addAll( getConstructorMetaData( beanClass ) );

		//TODO GM: currently class level constraints are represented by a PropertyMetaData. This
		//works but seems somewhat unnatural
		Set<MetaConstraint<?>> classLevelConstraints = getClassLevelConstraints( beanClass );
		if ( !classLevelConstraints.isEmpty() ) {
			ConstrainedType classLevelMetaData =
					new ConstrainedType(
							ConfigurationSource.ANNOTATION,
							ConstraintLocation.forClass( beanClass ),
							classLevelConstraints
					);
			constrainedElements.add( classLevelMetaData );
		}

		return new BeanConfiguration<T>(
				ConfigurationSource.ANNOTATION,
				beanClass,
				constrainedElements,
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
			@SuppressWarnings("unchecked")
			Class<? extends DefaultGroupSequenceProvider<? super T>> providerClass =
					(Class<? extends DefaultGroupSequenceProvider<? super T>>) groupSequenceProviderAnnotation.value();
			return newGroupSequenceProviderClassInstance( beanClass, providerClass );
		}

		return null;
	}

	private <T> DefaultGroupSequenceProvider<? super T> newGroupSequenceProviderClassInstance(Class<T> beanClass,
			Class<? extends DefaultGroupSequenceProvider<? super T>> providerClass) {
		Method[] providerMethods = run( GetMethods.action( providerClass ) );
		for ( Method method : providerMethods ) {
			Class<?>[] paramTypes = method.getParameterTypes();
			if ( "getValidationGroups".equals( method.getName() ) && !method.isBridge()
					&& paramTypes.length == 1 && paramTypes[0].isAssignableFrom( beanClass ) ) {

				return run(
						NewInstance.action( providerClass, "the default group sequence provider" )
				);
			}
		}

		throw log.getWrongDefaultGroupSequenceProviderTypeException( beanClass.getName() );
	}

	private Set<MetaConstraint<?>> getClassLevelConstraints(Class<?> clazz) {
		if ( annotationProcessingOptions.areClassLevelConstraintsIgnoredFor( clazz ) ) {
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

	private Set<ConstrainedElement> getFieldMetaData(Class<?> beanClass) {
		Set<ConstrainedElement> propertyMetaData = newHashSet();

		for ( Field field : run( GetDeclaredFields.action ( beanClass ) ) ) {
			// HV-172
			if ( Modifier.isStatic( field.getModifiers() ) ||
					annotationProcessingOptions.areMemberConstraintsIgnoredFor( field ) ||
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

		Map<Class<?>, Class<?>> groupConversions = getGroupConversions(
				field.getAnnotation( ConvertGroup.class ),
				field.getAnnotation( ConvertGroup.List.class )
		);

		boolean isCascading = field.isAnnotationPresent( Valid.class );
		boolean requiresUnwrapping = field.isAnnotationPresent( UnwrapValidatedValue.class );

		return new ConstrainedField(
				ConfigurationSource.ANNOTATION,
				ConstraintLocation.forProperty( field ),
				constraints,
				groupConversions,
				isCascading,
				requiresUnwrapping
		);
	}

	private Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, Field field) {
		Set<MetaConstraint<?>> constraints = newHashSet();

		for ( ConstraintDescriptorImpl<?> constraintDescription : constraintDescriptors ) {
			constraints.add( createMetaConstraint( field, constraintDescription ) );
		}
		return constraints;
	}

	private Set<ConstrainedExecutable> getConstructorMetaData(Class<?> clazz) {
		List<ExecutableElement> declaredConstructors = ExecutableElement.forConstructors(
				run( GetDeclaredConstructors.action( clazz ) )
		);

		return getMetaData( declaredConstructors );
	}

	private Set<ConstrainedExecutable> getMethodMetaData(Class<?> clazz) {
		List<ExecutableElement> declaredMethods = ExecutableElement.forMethods(
				run( GetDeclaredMethods.action( clazz ) )
		);

		return getMetaData( declaredMethods );
	}

	private Set<ConstrainedExecutable> getMetaData(List<ExecutableElement> executableElements) {
		Set<ConstrainedExecutable> executableMetaData = newHashSet();

		for ( ExecutableElement executable : executableElements ) {
			// HV-172; ignoring synthetic methods (inserted by the compiler), as they can't have any constraints
			// anyway and possibly hide the actual method with the same signature in the built meta model
			Member member = executable.getMember();
			if ( Modifier.isStatic( member.getModifiers() ) || member.isSynthetic() ) {
				continue;
			}

			executableMetaData.add( findExecutableMetaData( executable ) );
		}

		return executableMetaData;
	}

	/**
	 * Finds all constraint annotations defined for the given method or constructor.
	 *
	 * @param executable The executable element to check for constraints annotations.
	 *
	 * @return A meta data object describing the constraints specified for the
	 *         given element.
	 */
	private ConstrainedExecutable findExecutableMetaData(ExecutableElement executable) {
		List<ConstrainedParameter> parameterConstraints = getParameterMetaData( executable );

		AccessibleObject member = executable.getAccessibleObject();

		Map<ConstraintType, List<ConstraintDescriptorImpl<?>>> executableConstraints = partition(
				findConstraints(
						executable.getMember(),
						executable.getElementType()
				), byType()
		);

		Set<MetaConstraint<?>> crossParameterConstraints;
		if ( annotationProcessingOptions.areCrossParameterConstraintsIgnoredFor( executable.getMember() ) ) {
			crossParameterConstraints = Collections.emptySet();
		}
		else {
			crossParameterConstraints = convertToMetaConstraints(
					executableConstraints.get( ConstraintType.CROSS_PARAMETER ),
					executable
			);
		}

		Set<MetaConstraint<?>> returnValueConstraints;
		Map<Class<?>, Class<?>> groupConversions;
		boolean isCascading;
		boolean requiresUnwrapping = false;
		if ( annotationProcessingOptions.areReturnValueConstraintsIgnoredFor( executable.getMember() ) ) {
			returnValueConstraints = Collections.emptySet();
			groupConversions = Collections.emptyMap();
			isCascading = false;
		}
		else {
			requiresUnwrapping = executable.getAccessibleObject().isAnnotationPresent( UnwrapValidatedValue.class );

			returnValueConstraints = convertToMetaConstraints(
					executableConstraints.get( ConstraintType.GENERIC ),
					executable
			);
			groupConversions = getGroupConversions(
					member.getAnnotation( ConvertGroup.class ),
					member.getAnnotation( ConvertGroup.List.class )
			);
			isCascading = executable.getAccessibleObject().isAnnotationPresent( Valid.class );
		}

		return new ConstrainedExecutable(
				ConfigurationSource.ANNOTATION,
				ConstraintLocation.forReturnValue( executable ),
				parameterConstraints,
				crossParameterConstraints,
				returnValueConstraints,
				groupConversions,
				isCascading,
				requiresUnwrapping
		);
	}

	private Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintsDescriptors, ExecutableElement executable) {
		if ( constraintsDescriptors == null ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> constraints = newHashSet();

		for ( ConstraintDescriptorImpl<?> constraintDescriptor : constraintsDescriptors ) {
			constraints.add(
					constraintDescriptor.getConstraintType() == ConstraintType.GENERIC ?
							createReturnValueMetaConstraint( executable, constraintDescriptor ) :
							createCrossParameterMetaConstraint( executable, constraintDescriptor )
			);
		}

		return constraints;
	}

	/**
	 * Retrieves constraint related meta data for the parameters of the given
	 * executable.
	 *
	 * @param executable The executable of interest.
	 *
	 * @return A list with parameter meta data for the given executable.
	 */
	private List<ConstrainedParameter> getParameterMetaData(ExecutableElement executable) {
		List<ConstrainedParameter> metaData = newArrayList();

		List<String> parameterNames = executable.getParameterNames( parameterNameProvider );
		int i = 0;
		for ( Annotation[] parameterAnnotations : executable.getParameterAnnotations() ) {
			boolean parameterIsCascading = false;
			String parameterName = parameterNames.get( i );
			Set<MetaConstraint<?>> parameterConstraints = newHashSet();
			ConvertGroup groupConversion = null;
			ConvertGroup.List groupConversionList = null;
			boolean requiresUnwrapping = false;

			if ( annotationProcessingOptions.areParameterConstraintsIgnoredFor( executable.getMember(), i ) ) {
				metaData.add(
						new ConstrainedParameter(
								ConfigurationSource.ANNOTATION,
								ConstraintLocation.forParameter( executable, i ),
								ReflectionHelper.typeOf( executable, i ),
								i,
								parameterName,
								parameterConstraints,
								getGroupConversions( groupConversion, groupConversionList ),
								false,
								false
						)
				);
				i++;
				continue;
			}

			for ( Annotation parameterAnnotation : parameterAnnotations ) {
				//1. mark parameter as cascading if this annotation is the @Valid annotation
				if ( parameterAnnotation.annotationType().equals( Valid.class ) ) {
					parameterIsCascading = true;
				}
				//2. determine group conversions
				else if ( parameterAnnotation.annotationType().equals( ConvertGroup.class ) ) {
					groupConversion = (ConvertGroup) parameterAnnotation;
				}
				else if ( parameterAnnotation.annotationType().equals( ConvertGroup.List.class ) ) {
					groupConversionList = (ConvertGroup.List) parameterAnnotation;
				}
				//3. unwrapping required?
				else if ( parameterAnnotation.annotationType().equals( UnwrapValidatedValue.class ) ) {
					requiresUnwrapping = true;
				}

				//4. collect constraints if this annotation is a constraint annotation
				List<ConstraintDescriptorImpl<?>> constraints = findConstraintAnnotations(
						executable.getMember(), parameterAnnotation, ElementType.PARAMETER
				);
				for ( ConstraintDescriptorImpl<?> constraintDescriptorImpl : constraints ) {
					parameterConstraints.add(
							createParameterMetaConstraint( executable, i, constraintDescriptorImpl )
					);
				}
			}

			metaData.add(
					new ConstrainedParameter(
							ConfigurationSource.ANNOTATION,
							ConstraintLocation.forParameter( executable, i ),
							ReflectionHelper.typeOf( executable, i ),
							i,
							parameterName,
							parameterConstraints,
							getGroupConversions( groupConversion, groupConversionList ),
							parameterIsCascading,
							requiresUnwrapping
					)
			);
			i++;
		}

		return metaData;
	}

	/**
	 * Finds all constraint annotations defined for the given member and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param member The member to check for constraints annotations.
	 * @param type The element type the constraint/annotation is placed on.
	 *
	 * @return A list of constraint descriptors for all constraint specified for the given member.
	 */
	private List<ConstraintDescriptorImpl<?>> findConstraints(Member member, ElementType type) {
		List<ConstraintDescriptorImpl<?>> metaData = newArrayList();
		for ( Annotation annotation : ( (AccessibleObject) member ).getDeclaredAnnotations() ) {
			metaData.addAll( findConstraintAnnotations( member, annotation, type ) );
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
		List<ConstraintDescriptorImpl<?>> metaData = newArrayList();
		for ( Annotation annotation : beanClass.getDeclaredAnnotations() ) {
			metaData.addAll( findConstraintAnnotations( null, annotation, ElementType.TYPE ) );
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
	private <A extends Annotation> List<ConstraintDescriptorImpl<?>> findConstraintAnnotations(Member member,
			A annotation,
			ElementType type) {
		List<ConstraintDescriptorImpl<?>> constraintDescriptors = newArrayList();

		List<Annotation> constraints = newArrayList();
		Class<? extends Annotation> annotationType = annotation.annotationType();
		if ( constraintHelper.isConstraintAnnotation( annotationType ) ) {
			constraints.add( annotation );
		}
		else if ( constraintHelper.isMultiValueConstraint( annotationType ) ) {
			constraints.addAll( constraintHelper.getConstraintsFromMultiValueConstraint( annotation ) );
		}

		for ( Annotation constraint : constraints ) {
			final ConstraintDescriptorImpl<?> constraintDescriptor = buildConstraintDescriptor(
					member, constraint, type
			);
			constraintDescriptors.add( constraintDescriptor );
		}
		return constraintDescriptors;
	}

	private Map<Class<?>, Class<?>> getGroupConversions(ConvertGroup groupConversion, ConvertGroup.List groupConversionList) {
		Map<Class<?>, Class<?>> groupConversions = newHashMap();

		if ( groupConversion != null ) {
			groupConversions.put( groupConversion.from(), groupConversion.to() );
		}

		if ( groupConversionList != null ) {
			for ( ConvertGroup conversion : groupConversionList.value() ) {
				if ( groupConversions.containsKey( conversion.from() ) ) {
					throw log.getMultipleGroupConversionsForSameSourceException(
							conversion.from(),
							CollectionHelper.<Class<?>>asSet(
									groupConversions.get( conversion.from() ),
									conversion.to()
							)
					);
				}

				groupConversions.put( conversion.from(), conversion.to() );
			}
		}

		return groupConversions;
	}

	private Partitioner<ConstraintType, ConstraintDescriptorImpl<?>> byType() {
		return new Partitioner<ConstraintType, ConstraintDescriptorImpl<?>>() {

			@Override
			public ConstraintType getPartition(ConstraintDescriptorImpl<?> v) {
				return v.getConstraintType();
			}
		};
	}

	private <A extends Annotation> MetaConstraint<?> createMetaConstraint(Class<?> declaringClass,
			ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<A>( descriptor, ConstraintLocation.forClass( declaringClass ) );
	}

	private <A extends Annotation> MetaConstraint<?> createMetaConstraint(Member member, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<A>( descriptor, ConstraintLocation.forProperty( member ) );
	}

	private <A extends Annotation> MetaConstraint<A> createParameterMetaConstraint(ExecutableElement member,
			int parameterIndex, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<A>(
				descriptor,
				ConstraintLocation.forParameter( member, parameterIndex )
		);
	}

	private <A extends Annotation> MetaConstraint<A> createReturnValueMetaConstraint(ExecutableElement member,
			ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<A>( descriptor, ConstraintLocation.forReturnValue( member ) );
	}

	private <A extends Annotation> MetaConstraint<A> createCrossParameterMetaConstraint(ExecutableElement member,
			ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<A>( descriptor, ConstraintLocation.forCrossParameter( member ) );
	}

	private <A extends Annotation> ConstraintDescriptorImpl<A> buildConstraintDescriptor(Member member,
			A annotation,
			ElementType type) {
		return new ConstraintDescriptorImpl<A>(
				constraintHelper,
				member,
				annotation,
				type
		);
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
