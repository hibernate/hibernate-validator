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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.GroupSequence;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;

import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.internal.engine.cascading.AnnotatedObject;
import org.hibernate.validator.internal.engine.valuehandling.UnwrapMode;
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
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;
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
	protected final ConcurrentReferenceHashMap<Class<?>, BeanConfiguration<?>> configuredBeans;
	protected final AnnotationProcessingOptions annotationProcessingOptions;
	protected final ExecutableParameterNameProvider parameterNameProvider;

	public AnnotationMetaDataProvider(ConstraintHelper constraintHelper,
			ExecutableParameterNameProvider parameterNameProvider,
			AnnotationProcessingOptions annotationProcessingOptions) {
		this.constraintHelper = constraintHelper;
		this.parameterNameProvider = parameterNameProvider;
		this.annotationProcessingOptions = annotationProcessingOptions;
		configuredBeans = new ConcurrentReferenceHashMap<>(
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
			classLevelConstraints.add( new MetaConstraint<>( constraintDescription, location ) );
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

		List<TypeVariable<?>> cascadingTypeVariables = findCascadingTypeParameters( field );
		Set<MetaConstraint<?>> typeArgumentsConstraints = findTypeAnnotationConstraintsForMember( field );

		boolean typeArgumentAnnotated = !typeArgumentsConstraints.isEmpty();
		UnwrapMode unwrapMode = unwrapMode( field, typeArgumentAnnotated );

		return new ConstrainedField(
				ConfigurationSource.ANNOTATION,
				field,
				constraints,
				typeArgumentsConstraints,
				groupConversions,
				cascadingTypeVariables,
				unwrapMode
		);
	}

	private UnwrapMode unwrapMode(Field field, boolean typeArgumentAnnotated) {
		boolean isCollection = ReflectionHelper.isCollection( ReflectionHelper.typeOf( field ) );
		UnwrapValidatedValue unwrapValidatedValue = field.getAnnotation( UnwrapValidatedValue.class );
		return unwrapMode( typeArgumentAnnotated, isCollection, unwrapValidatedValue );
	}

	private UnwrapMode unwrapMode(Executable executable, boolean typeArgumentAnnotated) {
		boolean isCollection = ReflectionHelper.isCollection( ReflectionHelper.typeOf( executable ) );
		UnwrapValidatedValue unwrapValidatedValue = executable.getAnnotation( UnwrapValidatedValue.class );
		return unwrapMode( typeArgumentAnnotated, isCollection, unwrapValidatedValue );
	}

	private Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, Field field) {
		if ( constraintDescriptors.isEmpty() ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> constraints = newHashSet();

		ConstraintLocation location = ConstraintLocation.forProperty( field );

		for ( ConstraintDescriptorImpl<?> constraintDescription : constraintDescriptors ) {
			constraints.add( new MetaConstraint<>( constraintDescription, location ) );
		}
		return constraints;
	}

	private UnwrapMode unwrapMode(boolean typeArgumentAnnotated, boolean isCollection, UnwrapValidatedValue unwrapValidatedValue) {
		if ( unwrapValidatedValue == null && typeArgumentAnnotated && !isCollection ) {
			/*
			 * Optional<@NotNull String> exampleValue
			 */
			return UnwrapMode.UNWRAP;
		}
		else if ( unwrapValidatedValue != null ) {
			/*
			 * @UnwrapValidatedValue(false) Optional<@NotNull String> exampleValue
			 */
			return unwrapValidatedValue.value() ? UnwrapMode.UNWRAP : UnwrapMode.SKIP_UNWRAP;
		}
		/*
		 * @NotNull Optional<String> exampleValue
		 *
		 * @NotNull String otherExampleValue
		 *
		 * @NotNull
		 * List<@NotBlankTypeUse String> thirdExampleValue
		 */
		return UnwrapMode.AUTOMATIC;
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
		List<TypeVariable<?>> cascadingTypeVariables;

		UnwrapMode unwrapMode = UnwrapMode.AUTOMATIC;
		if ( annotationProcessingOptions.areReturnValueConstraintsIgnoredFor( executable ) ) {
			returnValueConstraints = Collections.emptySet();
			typeArgumentsConstraints = Collections.<MetaConstraint<?>>emptySet();
			groupConversions = Collections.emptyMap();
			cascadingTypeVariables = Collections.emptyList();
		}
		else {
			typeArgumentsConstraints = findTypeAnnotationConstraintsForMember( executable );
			boolean typeArgumentAnnotated = !typeArgumentsConstraints.isEmpty();
			unwrapMode = unwrapMode( executable, typeArgumentAnnotated );
			returnValueConstraints = convertToMetaConstraints(
					executableConstraints.get( ConstraintType.GENERIC ),
					executable
			);
			groupConversions = getGroupConversions(
					executable.getAnnotation( ConvertGroup.class ),
					executable.getAnnotation( ConvertGroup.List.class )
			);
			cascadingTypeVariables = findCascadingTypeParameters( executable );
		}

		return new ConstrainedExecutable(
				ConfigurationSource.ANNOTATION,
				executable,
				parameterConstraints,
				crossParameterConstraints,
				returnValueConstraints,
				typeArgumentsConstraints,
				groupConversions,
				cascadingTypeVariables,
				unwrapMode
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
			constraints.add(
					new MetaConstraint<>(
							constraintDescriptor,
							constraintDescriptor.getConstraintType() == ConstraintType.GENERIC ? returnValueLocation : crossParameterLocation
					)
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
	private List<ConstrainedParameter> getParameterMetaData(Executable executable) {
		List<ConstrainedParameter> metaData = newArrayList();

		List<String> parameterNames = parameterNameProvider.getParameterNames( executable );

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

			String parameterName = parameterNames.get( i );
			Set<MetaConstraint<?>> parameterConstraints = newHashSet();
			Set<MetaConstraint<?>> typeArgumentsConstraints = newHashSet();
			ConvertGroup groupConversion = null;
			ConvertGroup.List groupConversionList = null;

			if ( annotationProcessingOptions.areParameterConstraintsIgnoredFor( executable, i ) ) {
				metaData.add(
						new ConstrainedParameter(
								ConfigurationSource.ANNOTATION,
								executable,
								ReflectionHelper.typeOf( executable, i ),
								i,
								parameterName,
								parameterConstraints,
								typeArgumentsConstraints,
								getGroupConversions( groupConversion, groupConversionList ),
								Collections.emptyList(),
								UnwrapMode.AUTOMATIC
						)
				);
				i++;
				continue;
			}

			UnwrapValidatedValue unwrapValidatedValue = null;

			ConstraintLocation location = ConstraintLocation.forParameter( executable, i );

			for ( Annotation parameterAnnotation : parameterAnnotations ) {
				// determine group conversions
				if ( parameterAnnotation.annotationType().equals( ConvertGroup.class ) ) {
					groupConversion = (ConvertGroup) parameterAnnotation;
				}
				else if ( parameterAnnotation.annotationType().equals( ConvertGroup.List.class ) ) {
					groupConversionList = (ConvertGroup.List) parameterAnnotation;
				}

				// unwrapping required?
				else if ( parameterAnnotation.annotationType().equals( UnwrapValidatedValue.class ) ) {
					unwrapValidatedValue = (UnwrapValidatedValue) parameterAnnotation;
				}

				// collect constraints if this annotation is a constraint annotation
				List<ConstraintDescriptorImpl<?>> constraints = findConstraintAnnotations(
						executable, parameterAnnotation, ElementType.PARAMETER
				);
				for ( ConstraintDescriptorImpl<?> constraintDescriptorImpl : constraints ) {
					parameterConstraints.add(
							new MetaConstraint<>( constraintDescriptorImpl, location )
					);
				}
			}

			typeArgumentsConstraints = findTypeAnnotationConstraintsForExecutableParameter( executable, i );
			List<TypeVariable<?>> cascadingTypeParameters = findCascadingTypeParameters( parameter );
			boolean typeArgumentAnnotated = !typeArgumentsConstraints.isEmpty();
			boolean isCollection = ReflectionHelper.isCollection( ReflectionHelper.typeOf( executable, i ) );
			UnwrapMode unwrapMode = unwrapMode( typeArgumentAnnotated, isCollection, unwrapValidatedValue );

			metaData.add(
					new ConstrainedParameter(
							ConfigurationSource.ANNOTATION,
							executable,
							ReflectionHelper.typeOf( executable, i ),
							i,
							parameterName,
							parameterConstraints,
							typeArgumentsConstraints,
							getGroupConversions( groupConversion, groupConversionList ),
							cascadingTypeParameters,
							unwrapMode
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
	 * Finds type arguments constraints for fields and methods return values.
	 *
	 * @param member the field or method
	 *
	 * @return a set of type arguments constraints, or an empty set if no constrained type arguments are found
	 */
	protected Set<MetaConstraint<?>> findTypeAnnotationConstraintsForMember(Member member) {
		AnnotatedType annotatedType = null;
		TypeArgumentLocation location = null;
		if ( member instanceof Field ) {
			annotatedType = ( (Field) member ).getAnnotatedType();
			location = new TypeArgumentPropertyLocation( member );
		}
		else if ( member instanceof Method ) {
			annotatedType = ( (Method) member ).getAnnotatedReturnType();
			location = new TypeArgumentReturnValueLocation( (Executable) member );
		}

		return findTypeArgumentsConstraints(
				member,
				location,
				annotatedType,
				( (AccessibleObject) member ).isAnnotationPresent( Valid.class )
		);
	}

	private List<TypeVariable<?>> findCascadingTypeParameters(Parameter parameter) {
		TypeVariable<?>[] typeParameters = parameter.getType().getTypeParameters();
		AnnotatedType annotatedType = parameter.getAnnotatedType();

		List<TypeVariable<?>> cascadingTypeParameters = getCascadingTypeParameters( typeParameters, annotatedType );

		if ( parameter.isAnnotationPresent( Valid.class ) ) {
			cascadingTypeParameters.add( AnnotatedObject.INSTANCE );
		}

		return cascadingTypeParameters;
	}

	private List<TypeVariable<?>> findCascadingTypeParameters(Field field) {
		TypeVariable<?>[] typeParameters = field.getType().getTypeParameters();
		AnnotatedType annotatedType = field.getAnnotatedType();

		List<TypeVariable<?>> cascadingTypeParameters = getCascadingTypeParameters( typeParameters, annotatedType );

		if ( field.isAnnotationPresent( Valid.class ) ) {
			cascadingTypeParameters.add( AnnotatedObject.INSTANCE );
		}

		return cascadingTypeParameters;
	}

	private List<TypeVariable<?>> findCascadingTypeParameters(Executable executable) {
		TypeVariable<?>[] typeParameters = executable instanceof Method ? ( (Method) executable ).getReturnType().getTypeParameters()
				: ( (Constructor<?>) executable ).getDeclaringClass().getTypeParameters();
		AnnotatedType annotatedType = executable.getAnnotatedReturnType();

		List<TypeVariable<?>> cascadingTypeParameters = getCascadingTypeParameters( typeParameters, annotatedType );

		if ( executable.isAnnotationPresent( Valid.class ) ) {
			cascadingTypeParameters.add( AnnotatedObject.INSTANCE );
		}

		return cascadingTypeParameters;
	}

	private List<TypeVariable<?>> getCascadingTypeParameters(TypeVariable<?>[] typeParameters, AnnotatedType annotatedType) {
		List<TypeVariable<?>> cascadingTypeParameters = new ArrayList<>();

		if ( annotatedType instanceof AnnotatedParameterizedType ) {
			AnnotatedParameterizedType annotatedParameterizedType = (AnnotatedParameterizedType) annotatedType;
			AnnotatedType[] annotatedTypeArguments = annotatedParameterizedType.getAnnotatedActualTypeArguments();

			int i = 0;

			for ( AnnotatedType annotatedTypeArgument : annotatedTypeArguments ) {
				if ( annotatedTypeArgument.isAnnotationPresent( Valid.class ) ) {
					cascadingTypeParameters.add( typeParameters[i] );
				}
				i++;
			}
		}

		return cascadingTypeParameters;
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
					parameter.isAnnotationPresent( Valid.class )
			);
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			log.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
			return Collections.emptySet();
		}
	}

	private Set<MetaConstraint<?>> findTypeArgumentsConstraints(Member member, TypeArgumentLocation location, AnnotatedType annotatedType, boolean isCascaded) {
		Optional<AnnotatedType> annotationCarryingTypeParameter = getAnnotationCarryingTypeParameter( annotatedType );
		if ( !annotationCarryingTypeParameter.isPresent() ) {
			return Collections.emptySet();
		}

		List<ConstraintDescriptorImpl<?>> constraintDescriptors = findTypeUseConstraints( member, annotationCarryingTypeParameter.get() );
		if ( constraintDescriptors.isEmpty() ) {
			return Collections.emptySet();
		}

		Type validatedType = annotatedType.getType();
		if ( ReflectionHelper.isCollection( annotatedType.getType() ) ) {
			// HV-925
			// We need to determine the validated type used for constraint validator resolution.
			// Iterables, maps and arrays need special treatment at this point, since the validated type is the type of
			// the specified type parameter. In the other cases the validated type is the parameterized type, eg
			// Optional<String>.
			// In the latter case a value unwrapping has to occur.

			if ( !isCascaded ) {
				throw log.getTypeAnnotationConstraintOnIterableRequiresUseOfValidAnnotationException(
						member.getDeclaringClass(),
						member.getName() );
			}

			if ( TypeHelper.isAssignable( AnnotatedArrayType.class, annotatedType.getClass() ) ) {
				// in the case of an array, we need to unwrap the type as it was not unwrapped before as
				// only the wrapper type carries the type annotations
				validatedType = ( (AnnotatedArrayType) annotationCarryingTypeParameter.get() ).getAnnotatedGenericComponentType().getType();
			}
			else {
				validatedType = annotationCarryingTypeParameter.get().getType();
			}
		}

		return convertToTypeArgumentMetaConstraints(
				constraintDescriptors,
				location,
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
	private Set<MetaConstraint<?>> convertToTypeArgumentMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, TypeArgumentLocation location, Type type) {
		Set<MetaConstraint<?>> constraints = newHashSet( constraintDescriptors.size() );
		for ( ConstraintDescriptorImpl<?> constraintDescription : constraintDescriptors ) {
			MetaConstraint<?> metaConstraint = createTypeArgumentMetaConstraint( constraintDescription, location, type );
			constraints.add( metaConstraint );
		}
		return constraints;
	}

	/**
	 * Creates a {@code MetaConstraint} for a type argument constraint.
	 */
	private <A extends Annotation> MetaConstraint<?> createTypeArgumentMetaConstraint(ConstraintDescriptorImpl<A> descriptor, TypeArgumentLocation location, Type type) {
		return new MetaConstraint<>( descriptor, ConstraintLocation.forTypeArgument( location.toConstraintLocation(), type ) );
	}

	/**
	 * Returns the type argument of a parameterized type. If the type is a {@code Map}, the method returns the value
	 * type argument. If the type has more than one type argument and is not a Map, the method returns an empty {@code
	 * Optional}.
	 */
	private Optional<AnnotatedType> getAnnotationCarryingTypeParameter(AnnotatedType annotatedType) {
		if ( annotatedType == null ) {
			return Optional.empty();
		}

		if ( TypeHelper.isAssignable( AnnotatedParameterizedType.class, annotatedType.getClass() ) ) {
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
				log.parameterizedTypeWithMoreThanOneTypeArgumentIsNotSupported( annotatedType.getType() );
			}
		}
		else if ( TypeHelper.isAssignable( AnnotatedArrayType.class, annotatedType.getClass() ) ) {
			// in the case of an array, the unwrapped type does not carry the annotations
			// so we have to return the wrapper type: we will unwrap it later
			return Optional.of( annotatedType );
		}

		return Optional.empty();
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

	private static class TypeArgumentPropertyLocation implements TypeArgumentLocation {
		private final Member member;

		private TypeArgumentPropertyLocation(Member member) {
			this.member = member;
		}

		@Override
		public ConstraintLocation toConstraintLocation() {
			return ConstraintLocation.forProperty( member );
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
}
