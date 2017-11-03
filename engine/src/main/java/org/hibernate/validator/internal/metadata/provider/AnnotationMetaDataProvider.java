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
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
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
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
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
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
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
 * @author Guillaume Smet
 */
public class AnnotationMetaDataProvider implements MetaDataProvider {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private static final Annotation[] EMPTY_PARAMETER_ANNOTATIONS = new Annotation[0];

	private final ConstraintHelper constraintHelper;
	private final TypeResolutionHelper typeResolutionHelper;
	private final AnnotationProcessingOptions annotationProcessingOptions;
	private final ValueExtractorManager valueExtractorManager;

	private final BeanConfiguration<Object> objectBeanConfiguration;

	public AnnotationMetaDataProvider(ConstraintHelper constraintHelper,
			TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager,
			AnnotationProcessingOptions annotationProcessingOptions) {
		this.constraintHelper = constraintHelper;
		this.typeResolutionHelper = typeResolutionHelper;
		this.valueExtractorManager = valueExtractorManager;
		this.annotationProcessingOptions = annotationProcessingOptions;

		this.objectBeanConfiguration = retrieveBeanConfiguration( Object.class );
	}

	@Override
	public AnnotationProcessingOptions getAnnotationProcessingOptions() {
		return new AnnotationProcessingOptionsImpl();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> BeanConfiguration<T> getBeanConfiguration(Class<T> beanClass) {
		if ( Object.class.equals( beanClass ) ) {
			return (BeanConfiguration<T>) objectBeanConfiguration;
		}

		return retrieveBeanConfiguration( beanClass );
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

		throw LOG.getWrongDefaultGroupSequenceProviderTypeException( beanClass );
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

		CascadingMetaDataBuilder cascadingMetaDataBuilder = findCascadingMetaData( field );
		Set<MetaConstraint<?>> typeArgumentsConstraints = findTypeAnnotationConstraints( field );

		return new ConstrainedField(
				ConfigurationSource.ANNOTATION,
				field,
				constraints,
				typeArgumentsConstraints,
				cascadingMetaDataBuilder
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
		CascadingMetaDataBuilder cascadingMetaDataBuilder;

		if ( annotationProcessingOptions.areReturnValueConstraintsIgnoredFor( executable ) ) {
			returnValueConstraints = Collections.emptySet();
			typeArgumentsConstraints = Collections.emptySet();
			cascadingMetaDataBuilder = CascadingMetaDataBuilder.nonCascading();
		}
		else {
			AnnotatedType annotatedReturnType = executable.getAnnotatedReturnType();

			typeArgumentsConstraints = findTypeAnnotationConstraints( executable, annotatedReturnType );
			returnValueConstraints = convertToMetaConstraints(
					executableConstraints.get( ConstraintType.GENERIC ),
					executable
			);
			cascadingMetaDataBuilder = findCascadingMetaData( executable, annotatedReturnType );
		}

		return new ConstrainedExecutable(
				ConfigurationSource.ANNOTATION,
				executable,
				parameterConstraints,
				crossParameterConstraints,
				returnValueConstraints,
				typeArgumentsConstraints,
				cascadingMetaDataBuilder
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
		if ( executable.getParameterCount() == 0 ) {
			return Collections.emptyList();
		}

		Parameter[] parameters = executable.getParameters();

		List<ConstrainedParameter> metaData = new ArrayList<>( parameters.length );

		int i = 0;
		for ( Parameter parameter : parameters ) {
			Annotation[] parameterAnnotations;
			try {
				parameterAnnotations = parameter.getAnnotations();
			}
			catch (ArrayIndexOutOfBoundsException ex) {
				LOG.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
				parameterAnnotations = EMPTY_PARAMETER_ANNOTATIONS;
			}

			Set<MetaConstraint<?>> parameterConstraints = newHashSet();

			if ( annotationProcessingOptions.areParameterConstraintsIgnoredFor( executable, i ) ) {
				Type type = ReflectionHelper.typeOf( executable, i );
				metaData.add(
						new ConstrainedParameter(
								ConfigurationSource.ANNOTATION,
								executable,
								type,
								i,
								parameterConstraints,
								Collections.emptySet(),
								CascadingMetaDataBuilder.nonCascading()
						)
				);
				i++;
				continue;
			}

			ConstraintLocation location = ConstraintLocation.forParameter( executable, i );

			for ( Annotation parameterAnnotation : parameterAnnotations ) {
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

			AnnotatedType parameterAnnotatedType = parameter.getAnnotatedType();

			Set<MetaConstraint<?>> typeArgumentsConstraints = findTypeAnnotationConstraintsForExecutableParameter( executable, i, parameterAnnotatedType );
			CascadingMetaDataBuilder cascadingMetaData = findCascadingMetaData( executable, parameters, i, parameterAnnotatedType );

			metaData.add(
					new ConstrainedParameter(
							ConfigurationSource.ANNOTATION,
							executable,
							ReflectionHelper.typeOf( executable, i ),
							i,
							parameterConstraints,
							typeArgumentsConstraints,
							cascadingMetaData
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
	 * @return A list of constraint descriptors or the empty list in case {@code annotation} is neither a
	 * single nor multi-valued annotation.
	 */
	protected <A extends Annotation> List<ConstraintDescriptorImpl<?>> findConstraintAnnotations(Member member,
			A annotation,
			ElementType type) {

		// HV-1049 and HV-1311 - Ignore annotations from the JDK (jdk.internal.* and java.*); They cannot be constraint
		// annotations so skip them right here, as for the proper check we'd need package access permission for
		// "jdk.internal" and "java".
		if ( constraintHelper.isJdkAnnotation( annotation.annotationType() ) ) {
			return Collections.emptyList();
		}

		List<Annotation> constraints = newArrayList();
		Class<? extends Annotation> annotationType = annotation.annotationType();
		if ( constraintHelper.isConstraintAnnotation( annotationType ) ) {
			constraints.add( annotation );
		}
		else if ( constraintHelper.isMultiValueConstraint( annotationType ) ) {
			constraints.addAll( constraintHelper.getConstraintsFromMultiValueConstraint( annotation ) );
		}

		return constraints.stream()
				.map( c -> buildConstraintDescriptor( member, c, type ) )
				.collect( Collectors.toList() );
	}

	private Map<Class<?>, Class<?>> getGroupConversions(AnnotatedElement annotatedElement) {
		return getGroupConversions(
				annotatedElement.getAnnotation( ConvertGroup.class ),
				annotatedElement.getAnnotation( ConvertGroup.List.class )
		);
	}

	private Map<Class<?>, Class<?>> getGroupConversions(ConvertGroup groupConversion, ConvertGroup.List groupConversionList) {
		Map<Class<?>, Class<?>> groupConversions = newHashMap();

		if ( groupConversion != null ) {
			groupConversions.put( groupConversion.from(), groupConversion.to() );
		}

		if ( groupConversionList != null ) {
			for ( ConvertGroup conversion : groupConversionList.value() ) {
				if ( groupConversions.containsKey( conversion.from() ) ) {
					throw LOG.getMultipleGroupConversionsForSameSourceException(
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
				new ConstraintAnnotationDescriptor<>( annotation ),
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
			field.getAnnotatedType()
		);
	}

	/**
	 * Finds type arguments constraints for method return values.
	 */
	protected Set<MetaConstraint<?>> findTypeAnnotationConstraints(Executable executable, AnnotatedType annotatedReturnType) {
		return findTypeArgumentsConstraints(
			executable,
			new TypeArgumentReturnValueLocation( executable ),
			annotatedReturnType
		);
	}

	private CascadingMetaDataBuilder findCascadingMetaData(Executable executable, Parameter[] parameters, int i, AnnotatedType parameterAnnotatedType) {
		Parameter parameter = parameters[i];
		TypeVariable<?>[] typeParameters = parameter.getType().getTypeParameters();

		Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData = getTypeParametersCascadingMetadata( parameterAnnotatedType,
				typeParameters );

		try {
			return getCascadingMetaData( ReflectionHelper.typeOf( parameter.getDeclaringExecutable(), i ),
					parameter, containerElementTypesCascadingMetaData );
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			LOG.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
			return CascadingMetaDataBuilder.nonCascading();
		}
	}

	private CascadingMetaDataBuilder findCascadingMetaData(Field field) {
		TypeVariable<?>[] typeParameters = field.getType().getTypeParameters();
		AnnotatedType annotatedType = field.getAnnotatedType();

		Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData = getTypeParametersCascadingMetadata( annotatedType, typeParameters );

		return getCascadingMetaData( ReflectionHelper.typeOf( field ), field, containerElementTypesCascadingMetaData );
	}

	private CascadingMetaDataBuilder findCascadingMetaData(Executable executable, AnnotatedType annotatedReturnType) {
		TypeVariable<?>[] typeParameters;

		if ( executable instanceof Method ) {
			typeParameters = ( (Method) executable ).getReturnType().getTypeParameters();
		}
		else {
			typeParameters = ( (Constructor<?>) executable ).getDeclaringClass().getTypeParameters();
		}

		Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData = getTypeParametersCascadingMetadata( annotatedReturnType,
				typeParameters );

		return getCascadingMetaData( ReflectionHelper.typeOf( executable ), executable, containerElementTypesCascadingMetaData );
	}

	private Map<TypeVariable<?>, CascadingMetaDataBuilder> getTypeParametersCascadingMetadata(AnnotatedType annotatedType,
			TypeVariable<?>[] typeParameters) {
		if ( annotatedType instanceof AnnotatedArrayType ) {
			return getTypeParametersCascadingMetaDataForArrayType( (AnnotatedArrayType) annotatedType );
		}
		else if ( annotatedType instanceof AnnotatedParameterizedType ) {
			return getTypeParametersCascadingMetaDataForParameterizedType( (AnnotatedParameterizedType) annotatedType, typeParameters );
		}
		else {
			return Collections.emptyMap();
		}
	}

	private Map<TypeVariable<?>, CascadingMetaDataBuilder> getTypeParametersCascadingMetaDataForParameterizedType(
			AnnotatedParameterizedType annotatedParameterizedType, TypeVariable<?>[] typeParameters) {
		Map<TypeVariable<?>, CascadingMetaDataBuilder> typeParametersCascadingMetadata = CollectionHelper.newHashMap( typeParameters.length );

		AnnotatedType[] annotatedTypeArguments = annotatedParameterizedType.getAnnotatedActualTypeArguments();
		int i = 0;

		for ( AnnotatedType annotatedTypeArgument : annotatedTypeArguments ) {
			Map<TypeVariable<?>, CascadingMetaDataBuilder> nestedTypeParametersCascadingMetadata = getTypeParametersCascadingMetaDataForAnnotatedType(
					annotatedTypeArgument );

			typeParametersCascadingMetadata.put( typeParameters[i], new CascadingMetaDataBuilder( annotatedParameterizedType.getType(), typeParameters[i],
					annotatedTypeArgument.isAnnotationPresent( Valid.class ), nestedTypeParametersCascadingMetadata,
					getGroupConversions( annotatedTypeArgument ) ) );
			i++;
		}

		return typeParametersCascadingMetadata;
	}

	private Map<TypeVariable<?>, CascadingMetaDataBuilder> getTypeParametersCascadingMetaDataForArrayType(AnnotatedArrayType annotatedArrayType) {
		// HV-1428 Container element support is disabled for arrays
		return Collections.emptyMap();
//		Map<TypeVariable<?>, CascadingTypeParameter> typeParametersCascadingMetadata = CollectionHelper.newHashMap( 1 );
//		AnnotatedType containerElementAnnotatedType = annotatedArrayType.getAnnotatedGenericComponentType();
//
//		Map<TypeVariable<?>, CascadingTypeParameter> nestedTypeParametersCascadingMetadata = getTypeParametersCascadingMetaDataForAnnotatedType(
//				containerElementAnnotatedType );
//
//		TypeVariable<?> arrayElement = new ArrayElement( annotatedArrayType );
//		typeParametersCascadingMetadata.put( arrayElement, new CascadingTypeParameter( annotatedArrayType.getType(),
//				arrayElement,
//				annotatedArrayType.isAnnotationPresent( Valid.class ),
//				nestedTypeParametersCascadingMetadata,
//				getGroupConversions( annotatedArrayType ) ) );
//
//		return typeParametersCascadingMetadata;
	}

	private Map<TypeVariable<?>, CascadingMetaDataBuilder> getTypeParametersCascadingMetaDataForAnnotatedType(AnnotatedType annotatedType) {
		if ( annotatedType instanceof AnnotatedArrayType ) {
			return getTypeParametersCascadingMetaDataForArrayType( (AnnotatedArrayType) annotatedType );
		}
		else if ( annotatedType instanceof AnnotatedParameterizedType ) {
			return getTypeParametersCascadingMetaDataForParameterizedType( (AnnotatedParameterizedType) annotatedType,
					ReflectionHelper.getClassFromType( annotatedType.getType() ).getTypeParameters() );
		}
		else {
			return Collections.emptyMap();
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
	protected Set<MetaConstraint<?>> findTypeAnnotationConstraintsForExecutableParameter(Executable executable, int i, AnnotatedType parameterAnnotatedType) {
		try {
			return findTypeArgumentsConstraints(
					executable,
					new TypeArgumentExecutableParameterLocation( executable, i ),
					parameterAnnotatedType
			);
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			LOG.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
			return Collections.emptySet();
		}
	}

	private Set<MetaConstraint<?>> findTypeArgumentsConstraints(Member member, TypeArgumentLocation location, AnnotatedType annotatedType) {
		// HV-1428 Container element support is disabled for arrays
		if ( !(annotatedType instanceof AnnotatedParameterizedType) ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> typeArgumentConstraints = new HashSet<>();

		// if we have an array, we need to unwrap the array first
		if ( annotatedType instanceof AnnotatedArrayType ) {
			AnnotatedArrayType annotatedArrayType = (AnnotatedArrayType) annotatedType;
			Type validatedType = annotatedArrayType.getAnnotatedGenericComponentType().getType();
			TypeVariable<?> arrayElementTypeArgument = new ArrayElement( annotatedArrayType );

			typeArgumentConstraints.addAll( findTypeUseConstraints( member, annotatedArrayType, arrayElementTypeArgument, location, validatedType ) );

			typeArgumentConstraints.addAll( findTypeArgumentsConstraints( member,
					new NestedTypeArgumentLocation( location, arrayElementTypeArgument, validatedType ),
					annotatedArrayType.getAnnotatedGenericComponentType() ) );
		}
		else if ( annotatedType instanceof AnnotatedParameterizedType ) {
			AnnotatedParameterizedType annotatedParameterizedType = (AnnotatedParameterizedType) annotatedType;

			int i = 0;
			for ( TypeVariable<?> typeVariable : ReflectionHelper.getClassFromType( annotatedType.getType() ).getTypeParameters() ) {
				AnnotatedType annotatedTypeParameter = annotatedParameterizedType.getAnnotatedActualTypeArguments()[i];

				// HV-925
				// We need to determine the validated type used for constraint validator resolution.
				// Iterables and maps need special treatment at this point, since the validated type is the type of the
				// specified type parameter. In the other cases the validated type is the parameterized type, eg Optional<String>.
				// In the latter case a value unwrapping has to occur
				Type validatedType = annotatedTypeParameter.getType();

				typeArgumentConstraints.addAll( findTypeUseConstraints( member, annotatedTypeParameter, typeVariable, location, validatedType ) );

				if ( validatedType instanceof ParameterizedType ) {
					typeArgumentConstraints.addAll( findTypeArgumentsConstraints( member,
							new NestedTypeArgumentLocation( location, typeVariable, validatedType ),
							annotatedTypeParameter ) );
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

	private CascadingMetaDataBuilder getCascadingMetaData(Type type, AnnotatedElement annotatedElement,
			Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData) {
		return CascadingMetaDataBuilder.annotatedObject( type, annotatedElement.isAnnotationPresent( Valid.class ), containerElementTypesCascadingMetaData,
						getGroupConversions( annotatedElement ) );
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
