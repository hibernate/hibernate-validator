/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.provider;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashMap;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.ConcurrentReferenceHashMap.ReferenceType.SOFT;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;

import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.internal.engine.cascading.ArrayElement;
import org.hibernate.validator.internal.engine.cascading.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.cascading.CascadingTypeParameter;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
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
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredConstructors;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredFields;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethods;
import org.hibernate.validator.internal.util.privilegedactions.GetMethods;
import org.hibernate.validator.internal.util.privilegedactions.NewInstance;
import org.hibernate.validator.spi.group.DefaultGroupSequenceProvider;

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

	protected final ConstraintHelper constraintHelper;
	protected final TypeResolutionHelper typeResolutionHelper;
	protected final ConcurrentReferenceHashMap<Class<?>, BeanConfiguration<?>> configuredBeans;
	protected final AnnotationProcessingOptions annotationProcessingOptions;
	protected final ValueExtractorManager valueExtractorManager;

	public AnnotationMetaDataProvider(ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager,
			AnnotationProcessingOptions annotationProcessingOptions) {
		this.constraintHelper = constraintHelper;
		this.typeResolutionHelper = typeResolutionHelper;
		this.valueExtractorManager = valueExtractorManager;
		this.annotationProcessingOptions = annotationProcessingOptions;
		this.configuredBeans = new ConcurrentReferenceHashMap<>(
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
	public <T> BeanConfiguration<T> getBeanConfiguration(Class<T> beanClass) {
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
							beanClass,
							classLevelConstraints
					);
			constrainedElements.add( classLevelMetaData );
		}

		return new BeanConfiguration<>(
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

		throw log.getWrongDefaultGroupSequenceProviderTypeException( beanClass );
	}

	private Set<MetaConstraint<?>> getClassLevelConstraints(Class<?> clazz) {
		if ( annotationProcessingOptions.areClassLevelConstraintsIgnoredFor( clazz ) ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> classLevelConstraints = newHashSet();

		// HV-262
		List<ConstraintDescriptorImpl<?>> classMetaData = findClassLevelConstraints( clazz );

		ConstraintLocation location = ConstraintLocation.forClass( clazz );

		for ( ConstraintDescriptorImpl<?> constraintDescription : classMetaData ) {
			classLevelConstraints.add( MetaConstraints.create( typeResolutionHelper, valueExtractorManager, constraintDescription, location ) );
		}

		return classLevelConstraints;
	}

	private Set<ConstrainedElement> getFieldMetaData(Class<?> beanClass) {
		Set<ConstrainedElement> propertyMetaData = newHashSet();

		for ( Field field : run( GetDeclaredFields.action( beanClass ) ) ) {
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

		List<CascadingTypeParameter> cascadingTypeParameters = findCascadingTypeParameters( field );
		Set<MetaConstraint<?>> typeArgumentsConstraints = findTypeAnnotationConstraints( field );

		return new ConstrainedField(
				ConfigurationSource.ANNOTATION,
				field,
				constraints,
				typeArgumentsConstraints,
				groupConversions,
				cascadingTypeParameters
		);
	}

	private Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, Field field) {
		if ( constraintDescriptors.isEmpty() ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> constraints = newHashSet();

		ConstraintLocation location = ConstraintLocation.forField( field );

		for ( ConstraintDescriptorImpl<?> constraintDescription : constraintDescriptors ) {
			constraints.add( MetaConstraints.create( typeResolutionHelper, valueExtractorManager, constraintDescription, location ) );
		}
		return constraints;
	}

	private Set<ConstrainedExecutable> getConstructorMetaData(Class<?> clazz) {
		Executable[] declaredConstructors = run( GetDeclaredConstructors.action( clazz ) );

		return getMetaData( declaredConstructors );
	}

	private Set<ConstrainedExecutable> getMethodMetaData(Class<?> clazz) {
		Executable[] declaredMethods = run( GetDeclaredMethods.action( clazz ) );

		return getMetaData( declaredMethods );
	}

	private Set<ConstrainedExecutable> getMetaData(Executable[] executableElements) {
		Set<ConstrainedExecutable> executableMetaData = newHashSet();

		for ( Executable executable : executableElements ) {
			// HV-172; ignoring synthetic methods (inserted by the compiler), as they can't have any constraints
			// anyway and possibly hide the actual method with the same signature in the built meta model
			if ( Modifier.isStatic( executable.getModifiers() ) || executable.isSynthetic() ) {
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
	 * given element.
	 */
	private ConstrainedExecutable findExecutableMetaData(Executable executable) {
		List<ConstrainedParameter> parameterConstraints = getParameterMetaData( executable );

		Map<ConstraintType, List<ConstraintDescriptorImpl<?>>> executableConstraints = findConstraints( executable, ExecutableHelper.getElementType( executable ) )
			.stream()
			.collect( Collectors.groupingBy( ConstraintDescriptorImpl::getConstraintType ) );

		Set<MetaConstraint<?>> crossParameterConstraints;
		if ( annotationProcessingOptions.areCrossParameterConstraintsIgnoredFor( executable ) ) {
			crossParameterConstraints = Collections.emptySet();
		}
		else {
			crossParameterConstraints = convertToMetaConstraints(
					executableConstraints.get( ConstraintType.CROSS_PARAMETER ),
					executable
			);
		}

		Set<MetaConstraint<?>> returnValueConstraints;
		Set<MetaConstraint<?>> typeArgumentsConstraints;
		Map<Class<?>, Class<?>> groupConversions;
		List<CascadingTypeParameter> cascadingTypeParameters;

		if ( annotationProcessingOptions.areReturnValueConstraintsIgnoredFor( executable ) ) {
			returnValueConstraints = Collections.emptySet();
			typeArgumentsConstraints = Collections.emptySet();
			groupConversions = Collections.emptyMap();
			cascadingTypeParameters = Collections.emptyList();
		}
		else {
			typeArgumentsConstraints = findTypeAnnotationConstraints( executable );
			returnValueConstraints = convertToMetaConstraints(
					executableConstraints.get( ConstraintType.GENERIC ),
					executable
			);
			groupConversions = getGroupConversions(
					executable.getAnnotation( ConvertGroup.class ),
					executable.getAnnotation( ConvertGroup.List.class )
			);
			cascadingTypeParameters = findCascadingTypeParameters( executable );
		}

		return new ConstrainedExecutable(
				ConfigurationSource.ANNOTATION,
				executable,
				parameterConstraints,
				crossParameterConstraints,
				returnValueConstraints,
				typeArgumentsConstraints,
				groupConversions,
				cascadingTypeParameters
		);
	}

	private Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintsDescriptors, Executable executable) {
		if ( constraintsDescriptors == null ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> constraints = newHashSet();

		ConstraintLocation returnValueLocation = ConstraintLocation.forReturnValue( executable );
		ConstraintLocation crossParameterLocation = ConstraintLocation.forCrossParameter( executable );

		for ( ConstraintDescriptorImpl<?> constraintDescriptor : constraintsDescriptors ) {
			ConstraintLocation location = constraintDescriptor.getConstraintType() == ConstraintType.GENERIC ? returnValueLocation : crossParameterLocation;
			constraints.add( MetaConstraints.create( typeResolutionHelper, valueExtractorManager, constraintDescriptor, location ) );
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
	private List<ConstrainedParameter> getParameterMetaData(Executable executable) {
		List<ConstrainedParameter> metaData = newArrayList();

		int i = 0;
		for ( Parameter parameter : executable.getParameters() ) {
			Annotation[] parameterAnnotations;
			try {
				parameterAnnotations = parameter.getAnnotations();
			}
			catch (ArrayIndexOutOfBoundsException ex) {
				log.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
				parameterAnnotations = new Annotation[0];
			}

			Set<MetaConstraint<?>> parameterConstraints = newHashSet();
			ConvertGroup groupConversion = null;
			ConvertGroup.List groupConversionList = null;

			if ( annotationProcessingOptions.areParameterConstraintsIgnoredFor( executable, i ) ) {
				metaData.add(
						new ConstrainedParameter(
								ConfigurationSource.ANNOTATION,
								executable,
								ReflectionHelper.typeOf( executable, i ),
								i,
								parameterConstraints,
								Collections.emptySet(),
								getGroupConversions( groupConversion, groupConversionList ),
								Collections.emptyList()
						)
				);
				i++;
				continue;
			}

			ConstraintLocation location = ConstraintLocation.forParameter( executable, i );

			for ( Annotation parameterAnnotation : parameterAnnotations ) {
				// determine group conversions
				if ( parameterAnnotation.annotationType().equals( ConvertGroup.class ) ) {
					groupConversion = (ConvertGroup) parameterAnnotation;
				}
				else if ( parameterAnnotation.annotationType().equals( ConvertGroup.List.class ) ) {
					groupConversionList = (ConvertGroup.List) parameterAnnotation;
				}

				// collect constraints if this annotation is a constraint annotation
				List<ConstraintDescriptorImpl<?>> constraints = findConstraintAnnotations(
						executable, parameterAnnotation, ElementType.PARAMETER
				);
				for ( ConstraintDescriptorImpl<?> constraintDescriptorImpl : constraints ) {
					parameterConstraints.add(
							MetaConstraints.create( typeResolutionHelper, valueExtractorManager, constraintDescriptorImpl, location )
					);
				}
			}

			Set<MetaConstraint<?>> typeArgumentsConstraints = findTypeAnnotationConstraintsForExecutableParameter( executable, i );
			List<CascadingTypeParameter> cascadingTypeParameters = findCascadingTypeParameters( executable, i );

			metaData.add(
					new ConstrainedParameter(
							ConfigurationSource.ANNOTATION,
							executable,
							ReflectionHelper.typeOf( executable, i ),
							i,
							parameterConstraints,
							typeArgumentsConstraints,
							getGroupConversions( groupConversion, groupConversionList ),
							cascadingTypeParameters
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
	 * @param member The member to check for constraints annotations
	 * @param annotation The annotation to examine
	 * @param type the element type on which the annotation/constraint is placed on
	 * @param <A> the annotation type
	 *
	 * @return A list of constraint descriptors or the empty list in case <code>annotation</code> is neither a
	 * single nor multi-valued annotation.
	 */
	protected <A extends Annotation> List<ConstraintDescriptorImpl<?>> findConstraintAnnotations(Member member,
			A annotation,
			ElementType type) {

		// HV-1049 Ignore annotations from jdk.internal.*; They cannot be constraint annotations so skip them right
		// here, as for the proper check we'd need package access permission for "jdk.internal"
		if ( isJdkInternalType( annotation ) ) {
			return Collections.emptyList();
		}

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

	private <A extends Annotation> boolean isJdkInternalType(A annotation) {
		Package pakkage = annotation.annotationType().getPackage();
		return pakkage != null && "jdk.internal".equals( pakkage.getName() );
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

	private <A extends Annotation> ConstraintDescriptorImpl<A> buildConstraintDescriptor(Member member,
			A annotation,
			ElementType type) {
		return new ConstraintDescriptorImpl<>(
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

	/**
	 * Finds type arguments constraints for fields.
	 */
	protected Set<MetaConstraint<?>> findTypeAnnotationConstraints(Field field) {
		return findTypeArgumentsConstraints(
			field,
			new TypeArgumentFieldLocation( field ),
			field.getAnnotatedType(),
			field.getType().getTypeParameters()
		);
	}

	/**
	 * Finds type arguments constraints for method return values.
	 */
	protected Set<MetaConstraint<?>> findTypeAnnotationConstraints(Executable executable) {
		TypeVariable<?>[] typeParameters;
		if ( executable instanceof Method ) {
			typeParameters = ( (Method) executable ).getReturnType().getTypeParameters();
		}
		else {
			typeParameters = new TypeVariable<?>[0];
		}

		return findTypeArgumentsConstraints(
			executable,
			new TypeArgumentReturnValueLocation( executable ),
			executable.getAnnotatedReturnType(),
			typeParameters
		);
	}

	private List<CascadingTypeParameter> findCascadingTypeParameters(Executable executable, int i) {
		Parameter parameter = executable.getParameters()[i];
		TypeVariable<?>[] typeParameters = parameter.getType().getTypeParameters();
		AnnotatedType annotatedType = parameter.getAnnotatedType();

		List<CascadingTypeParameter> cascadingTypeParameters = getCascadingTypeParameters( typeParameters, annotatedType );

		try {
			if ( parameter.isAnnotationPresent( Valid.class ) ) {
				cascadingTypeParameters.add( parameter.getType().isArray()
						? CascadingTypeParameter.arrayElement( ReflectionHelper.typeOf( parameter.getDeclaringExecutable(), i ) )
						: CascadingTypeParameter.annotatedObject( ReflectionHelper.typeOf( parameter.getDeclaringExecutable(), i ) ) );
			}
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			log.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
		}

		return cascadingTypeParameters.isEmpty() ? Collections.emptyList() : cascadingTypeParameters;
	}

	private List<CascadingTypeParameter> findCascadingTypeParameters(Field field) {
		TypeVariable<?>[] typeParameters = field.getType().getTypeParameters();
		AnnotatedType annotatedType = field.getAnnotatedType();

		List<CascadingTypeParameter> cascadingTypeParameters = getCascadingTypeParameters( typeParameters, annotatedType );

		if ( field.isAnnotationPresent( Valid.class ) ) {
			cascadingTypeParameters.add( field.getType().isArray()
					? CascadingTypeParameter.arrayElement( ReflectionHelper.typeOf( field ) )
					: CascadingTypeParameter.annotatedObject( ReflectionHelper.typeOf( field ) ) );
		}

		return cascadingTypeParameters.isEmpty() ? Collections.emptyList() : cascadingTypeParameters;
	}

	private List<CascadingTypeParameter> findCascadingTypeParameters(Executable executable) {
		boolean isArray;
		TypeVariable<?>[] typeParameters;

		if ( executable instanceof Method ) {
			isArray =  ( (Method) executable ).getReturnType().isArray();
			typeParameters = ( (Method) executable ).getReturnType().getTypeParameters();
		}
		else {
			isArray = false;
			typeParameters = ( (Constructor<?>) executable ).getDeclaringClass().getTypeParameters();
		}
		AnnotatedType annotatedType = executable.getAnnotatedReturnType();

		List<CascadingTypeParameter> cascadingTypeParameters = getCascadingTypeParameters( typeParameters, annotatedType );

		if ( executable.isAnnotationPresent( Valid.class ) ) {
			cascadingTypeParameters.add( isArray
					? CascadingTypeParameter.arrayElement( ReflectionHelper.typeOf( executable ) )
					: CascadingTypeParameter.annotatedObject( ReflectionHelper.typeOf( executable ) ) );
		}

		return cascadingTypeParameters.isEmpty() ? Collections.emptyList() : cascadingTypeParameters;
	}

	private List<CascadingTypeParameter> getCascadingTypeParameters(TypeVariable<?>[] typeParameters, AnnotatedType annotatedType) {
		List<CascadingTypeParameter> cascadingTypeParameters = new ArrayList<>();

		if ( annotatedType instanceof AnnotatedArrayType ) {
			addCascadingTypeParametersForArrayType( cascadingTypeParameters, (AnnotatedArrayType) annotatedType );
		}
		else if ( annotatedType instanceof AnnotatedParameterizedType ) {
			addCascadingTypeParametersForParameterizedType( cascadingTypeParameters, (AnnotatedParameterizedType) annotatedType, typeParameters );
		}

		return cascadingTypeParameters;
	}

	private void addCascadingTypeParametersForParameterizedType(List<CascadingTypeParameter> cascadingTypeParameters,
			AnnotatedParameterizedType annotatedParameterizedType, TypeVariable<?>[] typeParameters) {
		AnnotatedType[] annotatedTypeArguments = annotatedParameterizedType.getAnnotatedActualTypeArguments();

		int i = 0;

		for ( AnnotatedType annotatedTypeArgument : annotatedTypeArguments ) {
			Type validatedType = annotatedTypeArgument.getType();
			List<CascadingTypeParameter> nestedCascadingTypeParameters;
			if ( validatedType instanceof ParameterizedType ) {
				nestedCascadingTypeParameters = getCascadingTypeParameters(
						ReflectionHelper.getClassFromType( validatedType ).getTypeParameters(), annotatedTypeArgument );
			}
			else {
				nestedCascadingTypeParameters = Collections.emptyList();
			}

			boolean isCascading = annotatedTypeArgument.isAnnotationPresent( Valid.class );

			if ( isCascading || !nestedCascadingTypeParameters.isEmpty() ) {
				CascadingTypeParameter cascadingTypeParameter = new CascadingTypeParameter( annotatedParameterizedType.getType(), typeParameters[i],
						isCascading, nestedCascadingTypeParameters );

				cascadingTypeParameters.add( cascadingTypeParameter );
			}
			i++;
		}
	}

	private void addCascadingTypeParametersForArrayType(List<CascadingTypeParameter> cascadingTypeParameters, AnnotatedArrayType annotatedArrayType) {
		Type validatedType = annotatedArrayType.getAnnotatedGenericComponentType().getType();

		List<CascadingTypeParameter> nestedCascadingTypeParameters;
		if ( validatedType instanceof ParameterizedType ) {
			nestedCascadingTypeParameters = getCascadingTypeParameters( ReflectionHelper.getClassFromType( validatedType ).getTypeParameters(),
					annotatedArrayType.getAnnotatedGenericComponentType() );
		}
		else {
			nestedCascadingTypeParameters = Collections.emptyList();
		}

		boolean isCascading = annotatedArrayType.isAnnotationPresent( Valid.class );

		CascadingTypeParameter cascadingTypeParameter = new CascadingTypeParameter( validatedType, ArrayElement.INSTANCE,
				isCascading, nestedCascadingTypeParameters );

		if ( isCascading || !nestedCascadingTypeParameters.isEmpty() ) {
			cascadingTypeParameters.add( cascadingTypeParameter );
		}
	}

	/**
	 * Finds type arguments constraints for parameters.
	 *
	 * @param executable the executable
	 * @param i the parameter index
	 *
	 * @return a set of type arguments constraints, or an empty set if no constrained type arguments are found
	 */
	protected Set<MetaConstraint<?>> findTypeAnnotationConstraintsForExecutableParameter(Executable executable, int i) {
		Parameter parameter = executable.getParameters()[i];
		try {
			return findTypeArgumentsConstraints(
					executable,
					new TypeArgumentExecutableParameterLocation( executable, i ),
					parameter.getAnnotatedType(),
					parameter.getType().getTypeParameters()
			);
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			log.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
			return Collections.emptySet();
		}
	}

	private Set<MetaConstraint<?>> findTypeArgumentsConstraints(Member member, TypeArgumentLocation location, AnnotatedType annotatedType, TypeVariable<?>[] typeParameters) {
		Set<MetaConstraint<?>> typeArgumentConstraints = new HashSet<>();

		AnnotatedType currentAnnotatedType = annotatedType;
		TypeVariable<?>[] currentTypeParameters = typeParameters;
		TypeArgumentLocation currentLocation = location;

		// if we have an array, we need to unwrap the array first
		if ( currentAnnotatedType instanceof AnnotatedArrayType ) {
			AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType) currentAnnotatedType;
			Type validatedType = annotatedArrayType.getAnnotatedGenericComponentType().getType();

			typeArgumentConstraints.addAll( findTypeUseConstraints( member, annotatedArrayType, ArrayElement.INSTANCE, location, validatedType ) );

			currentAnnotatedType = annotatedArrayType.getAnnotatedGenericComponentType();
			if ( !( currentAnnotatedType instanceof AnnotatedParameterizedType ) ) {
				// return fast if we don't have to go further (the ReflectionHelper.getClassFromType call will fail on primitive types)
				return typeArgumentConstraints.isEmpty() ? Collections.emptySet() : typeArgumentConstraints;
			}
			else {
				currentTypeParameters = ReflectionHelper.getClassFromType( validatedType ).getTypeParameters();
				currentLocation = new NestedTypeArgumentLocation( location, ArrayElement.INSTANCE, validatedType );
			}
		}

		if ( currentAnnotatedType instanceof AnnotatedParameterizedType ) {
			AnnotatedParameterizedType annotatedParameterizedType = (AnnotatedParameterizedType) currentAnnotatedType;

			int i = 0;
			for ( TypeVariable<?> typeVariable : currentTypeParameters ) {
				AnnotatedType annotatedTypeParameter = annotatedParameterizedType.getAnnotatedActualTypeArguments()[i];

				// HV-925
				// We need to determine the validated type used for constraint validator resolution.
				// Iterables and maps need special treatment at this point, since the validated type is the type of the
				// specified type parameter. In the other cases the validated type is the parameterized type, eg Optional<String>.
				// In the latter case a value unwrapping has to occur
				Type validatedType = annotatedTypeParameter.getType();

				typeArgumentConstraints.addAll( findTypeUseConstraints( member, annotatedTypeParameter, typeVariable, currentLocation, validatedType ) );

				if ( validatedType instanceof ParameterizedType ) {
					typeArgumentConstraints.addAll( findTypeArgumentsConstraints( member,
							new NestedTypeArgumentLocation( currentLocation, typeVariable, validatedType ),
							annotatedTypeParameter,
							ReflectionHelper.getClassFromType( validatedType ).getTypeParameters() ) );
				}

				i++;
			}
		}

		return typeArgumentConstraints.isEmpty() ? Collections.emptySet() : typeArgumentConstraints;
	}

	/**
	 * Finds type use annotation constraints defined on the type argument.
	 */
	private Set<MetaConstraint<?>> findTypeUseConstraints(Member member, AnnotatedType typeArgument, TypeVariable<?> typeVariable, TypeArgumentLocation location, Type type) {
		Set<MetaConstraint<?>> constraints = Arrays.stream( typeArgument.getAnnotations() )
				.flatMap( a -> findConstraintAnnotations( member, a, ElementType.TYPE_USE ).stream() )
				.map( d -> createTypeArgumentMetaConstraint( d, location, typeVariable, type ) )
				.collect( Collectors.toSet() );

		return constraints;
	}

	/**
	 * Creates a {@code MetaConstraint} for a type argument constraint.
	 */
	private <A extends Annotation> MetaConstraint<?> createTypeArgumentMetaConstraint(ConstraintDescriptorImpl<A> descriptor, TypeArgumentLocation location,
			TypeVariable<?> typeVariable, Type type) {
		ConstraintLocation constraintLocation = ConstraintLocation.forTypeArgument( location.toConstraintLocation(), typeVariable, type );
		return MetaConstraints.create( typeResolutionHelper, valueExtractorManager, descriptor, constraintLocation );
	}

	/**
	 * The location of a type argument before it is really considered a constraint location.
	 * <p>
	 * It avoids initializing a constraint location if we did not find any constraints. This is especially useful in
	 * a Java 9 environment as {@link ConstraintLocation#forProperty(Member) tries to make the {@code Member} accessible
	 * which might not be possible (for instance for {@code java.util} classes).
	 */
	private interface TypeArgumentLocation {
		ConstraintLocation toConstraintLocation();
	}

	private static class TypeArgumentExecutableParameterLocation implements TypeArgumentLocation {
		private final Executable executable;

		private final int index;

		private TypeArgumentExecutableParameterLocation(Executable executable, int index) {
			this.executable = executable;
			this.index = index;
		}

		@Override
		public ConstraintLocation toConstraintLocation() {
			return ConstraintLocation.forParameter( executable, index );
		}
	}

	private static class TypeArgumentFieldLocation implements TypeArgumentLocation {
		private final Field field;

		private TypeArgumentFieldLocation(Field field) {
			this.field = field;
		}

		@Override
		public ConstraintLocation toConstraintLocation() {
			return ConstraintLocation.forField( field );
		}
	}

	private static class TypeArgumentReturnValueLocation implements TypeArgumentLocation {
		private final Executable executable;

		private TypeArgumentReturnValueLocation(Executable executable) {
			this.executable = executable;
		}

		@Override
		public ConstraintLocation toConstraintLocation() {
			return ConstraintLocation.forReturnValue( executable );
		}
	}

	private static class NestedTypeArgumentLocation implements TypeArgumentLocation {
		private final TypeArgumentLocation parentLocation;
		private final TypeVariable<?> typeParameter;
		private final Type typeOfAnnotatedElement;

		private NestedTypeArgumentLocation(TypeArgumentLocation parentLocation, TypeVariable<?> typeParameter, Type typeOfAnnotatedElement) {
			this.parentLocation = parentLocation;
			this.typeParameter = typeParameter;
			this.typeOfAnnotatedElement = typeOfAnnotatedElement;
		}

		@Override
		public ConstraintLocation toConstraintLocation() {
			return ConstraintLocation.forTypeArgument( parentLocation.toConstraintLocation(), typeParameter, typeOfAnnotatedElement );
		}

	}
}
