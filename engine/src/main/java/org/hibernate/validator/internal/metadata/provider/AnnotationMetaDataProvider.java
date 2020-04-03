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
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

import jakarta.validation.GroupSequence;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;

import org.hibernate.validator.group.GroupSequenceProvider;
import org.hibernate.validator.internal.engine.ConstraintCreationContext;
import org.hibernate.validator.internal.engine.valueextraction.ArrayElement;
import org.hibernate.validator.internal.metadata.aggregated.CascadingMetaDataBuilder;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptionsImpl;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.core.MetaConstraints;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl.ConstraintType;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation.ConstraintLocationKind;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.metadata.raw.ConstrainedType;
import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Constrainable;
import org.hibernate.validator.internal.properties.Getter;
import org.hibernate.validator.internal.properties.javabean.JavaBeanAnnotatedConstrainable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanAnnotatedElement;
import org.hibernate.validator.internal.properties.javabean.JavaBeanExecutable;
import org.hibernate.validator.internal.properties.javabean.JavaBeanField;
import org.hibernate.validator.internal.properties.javabean.JavaBeanHelper;
import org.hibernate.validator.internal.properties.javabean.JavaBeanParameter;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ReflectionHelper;
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

	private final ConstraintCreationContext constraintCreationContext;
	private final AnnotationProcessingOptions annotationProcessingOptions;
	private final JavaBeanHelper javaBeanHelper;

	private final BeanConfiguration<Object> objectBeanConfiguration;

	public AnnotationMetaDataProvider(ConstraintCreationContext constraintCreationContext,
			JavaBeanHelper javaBeanHelper,
			AnnotationProcessingOptions annotationProcessingOptions) {
		this.constraintCreationContext = constraintCreationContext;
		this.javaBeanHelper = javaBeanHelper;
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
			if ( "getValidationGroups".equals( method.getName() ) && !method.isBridge()
					&& method.getParameterCount() == 1 && method.getParameterTypes()[0].isAssignableFrom( beanClass ) ) {

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

		List<ConstraintDescriptorImpl<?>> classLevelConstraintDescriptors = findConstraints( null, clazz.getDeclaredAnnotations(),
				ConstraintLocationKind.TYPE );

		if ( classLevelConstraintDescriptors.isEmpty() ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> classLevelConstraints = newHashSet( classLevelConstraintDescriptors.size() );
		ConstraintLocation location = ConstraintLocation.forClass( clazz );

		for ( ConstraintDescriptorImpl<?> constraintDescriptor : classLevelConstraintDescriptors ) {
			classLevelConstraints.add( MetaConstraints.create( constraintCreationContext.getTypeResolutionHelper(),
					constraintCreationContext.getValueExtractorManager(),
					constraintCreationContext.getConstraintValidatorManager(),
					constraintDescriptor,
					location ) );
		}

		return classLevelConstraints;
	}

	private Set<ConstrainedElement> getFieldMetaData(Class<?> beanClass) {
		Set<ConstrainedElement> propertyMetaData = newHashSet();

		for ( Field field : run( GetDeclaredFields.action( beanClass ) ) ) {
			// HV-172
			if ( Modifier.isStatic( field.getModifiers() ) || field.isSynthetic() ) {
				continue;
			}

			JavaBeanField javaBeanField = javaBeanHelper.field( field );

			if ( annotationProcessingOptions.areMemberConstraintsIgnoredFor( javaBeanField ) ) {
				continue;
			}

			propertyMetaData.add( findPropertyMetaData( javaBeanField ) );
		}
		return propertyMetaData;
	}

	private ConstrainedField findPropertyMetaData(JavaBeanField javaBeanField) {
		Set<MetaConstraint<?>> constraints = convertToMetaConstraints(
				findConstraints( javaBeanField, ConstraintLocationKind.FIELD ),
				javaBeanField
		);

		CascadingMetaDataBuilder cascadingMetaDataBuilder = findCascadingMetaData( javaBeanField );
		Set<MetaConstraint<?>> typeArgumentsConstraints = findTypeAnnotationConstraints( javaBeanField );

		return new ConstrainedField(
				ConfigurationSource.ANNOTATION,
				javaBeanField,
				constraints,
				typeArgumentsConstraints,
				cascadingMetaDataBuilder
		);
	}

	private Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, JavaBeanField javaBeanField) {
		if ( constraintDescriptors.isEmpty() ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> constraints = newHashSet( constraintDescriptors.size() );

		ConstraintLocation location = ConstraintLocation.forField( javaBeanField );

		for ( ConstraintDescriptorImpl<?> constraintDescription : constraintDescriptors ) {
			constraints.add( MetaConstraints.create( constraintCreationContext.getTypeResolutionHelper(),
					constraintCreationContext.getValueExtractorManager(),
					constraintCreationContext.getConstraintValidatorManager(),
					constraintDescription, location ) );
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
		JavaBeanExecutable<?> javaBeanExecutable = javaBeanHelper.executable( executable );
		List<ConstrainedParameter> parameterConstraints = getParameterMetaData( javaBeanExecutable );

		Map<ConstraintType, List<ConstraintDescriptorImpl<?>>> executableConstraints = findConstraints(
				javaBeanExecutable,
				ConstraintLocationKind.of( javaBeanExecutable.getConstrainedElementKind() )
		).stream().collect( Collectors.groupingBy( ConstraintDescriptorImpl::getConstraintType ) );

		Set<MetaConstraint<?>> crossParameterConstraints;
		if ( annotationProcessingOptions.areCrossParameterConstraintsIgnoredFor( javaBeanExecutable ) ) {
			crossParameterConstraints = Collections.emptySet();
		}
		else {
			crossParameterConstraints = convertToMetaConstraints(
					executableConstraints.get( ConstraintType.CROSS_PARAMETER ),
					javaBeanExecutable
			);
		}

		Set<MetaConstraint<?>> returnValueConstraints;
		Set<MetaConstraint<?>> typeArgumentsConstraints;
		CascadingMetaDataBuilder cascadingMetaDataBuilder;

		if ( annotationProcessingOptions.areReturnValueConstraintsIgnoredFor( javaBeanExecutable ) ) {
			returnValueConstraints = Collections.emptySet();
			typeArgumentsConstraints = Collections.emptySet();
			cascadingMetaDataBuilder = CascadingMetaDataBuilder.nonCascading();
		}
		else {
			typeArgumentsConstraints = findTypeAnnotationConstraints( javaBeanExecutable );
			returnValueConstraints = convertToMetaConstraints(
					executableConstraints.get( ConstraintType.GENERIC ),
					javaBeanExecutable
			);
			cascadingMetaDataBuilder = findCascadingMetaData( javaBeanExecutable );
		}

		return new ConstrainedExecutable(
				ConfigurationSource.ANNOTATION,
				javaBeanExecutable,
				parameterConstraints,
				crossParameterConstraints,
				returnValueConstraints,
				typeArgumentsConstraints,
				cascadingMetaDataBuilder
		);
	}

	private Set<MetaConstraint<?>> convertToMetaConstraints(List<ConstraintDescriptorImpl<?>> constraintDescriptors, Callable callable) {
		if ( constraintDescriptors == null || constraintDescriptors.isEmpty() ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> constraints = newHashSet( constraintDescriptors.size() );

		ConstraintLocation returnValueLocation = ConstraintLocation.forReturnValue( callable );
		ConstraintLocation crossParameterLocation = ConstraintLocation.forCrossParameter( callable );

		for ( ConstraintDescriptorImpl<?> constraintDescriptor : constraintDescriptors ) {
			ConstraintLocation location = constraintDescriptor.getConstraintType() == ConstraintType.GENERIC
					? returnValueLocation
					: crossParameterLocation;
			constraints.add( MetaConstraints.create( constraintCreationContext.getTypeResolutionHelper(),
					constraintCreationContext.getValueExtractorManager(),
					constraintCreationContext.getConstraintValidatorManager(), constraintDescriptor, location ) );
		}

		return constraints;
	}

	/**
	 * Retrieves constraint related meta data for the parameters of the given
	 * executable.
	 *
	 * @param javaBeanExecutable The executable of interest.
	 *
	 * @return A list with parameter meta data for the given executable.
	 */
	private List<ConstrainedParameter> getParameterMetaData(JavaBeanExecutable<?> javaBeanExecutable) {
		if ( !javaBeanExecutable.hasParameters() ) {
			return Collections.emptyList();
		}

		List<JavaBeanParameter> parameters = javaBeanExecutable.getParameters();

		List<ConstrainedParameter> metaData = new ArrayList<>( parameters.size() );

		int i = 0;
		for ( JavaBeanParameter parameter : parameters ) {
			if ( annotationProcessingOptions.areParameterConstraintsIgnoredFor( javaBeanExecutable, i ) ) {
				metaData.add(
						new ConstrainedParameter(
								ConfigurationSource.ANNOTATION,
								javaBeanExecutable,
								parameter.getGenericType(),
								i,
								Collections.emptySet(),
								Collections.emptySet(),
								CascadingMetaDataBuilder.nonCascading()
						)
				);
				i++;
				continue;
			}

			List<ConstraintDescriptorImpl<?>> constraintDescriptors = findConstraints( javaBeanExecutable, parameter, ConstraintLocationKind.PARAMETER );
			Set<MetaConstraint<?>> parameterConstraints;

			if ( !constraintDescriptors.isEmpty() ) {
				parameterConstraints = newHashSet( constraintDescriptors.size() );
				ConstraintLocation location = ConstraintLocation.forParameter( javaBeanExecutable, i );

				for ( ConstraintDescriptorImpl<?> constraintDescriptorImpl : constraintDescriptors ) {
					parameterConstraints.add(
							MetaConstraints.create( constraintCreationContext.getTypeResolutionHelper(),
									constraintCreationContext.getValueExtractorManager(),
									constraintCreationContext.getConstraintValidatorManager(), constraintDescriptorImpl,
									location ) );
				}
			}
			else {
				parameterConstraints = Collections.emptySet();
			}

			Set<MetaConstraint<?>> typeArgumentsConstraints = findTypeAnnotationConstraintsForExecutableParameter( javaBeanExecutable, parameter );
			CascadingMetaDataBuilder cascadingMetaData = findCascadingMetaData( parameter );

			metaData.add(
					new ConstrainedParameter(
							ConfigurationSource.ANNOTATION,
							javaBeanExecutable,
							parameter.getGenericType(),
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
	 * Finds all constraint annotations defined for the given constrainable and returns them in a list of
	 * constraint descriptors.
	 *
	 * @param constrainable The constrainable to check for constraint annotations.
	 * @param kind The constraint location kind.
	 *
	 * @return A list of constraint descriptors for all constraint specified for the given member.
	 */
	private List<ConstraintDescriptorImpl<?>> findConstraints(JavaBeanAnnotatedConstrainable constrainable, ConstraintLocationKind kind) {
		return findConstraints( constrainable, constrainable, kind );
	}

	/**
	 * Finds all constraint annotations defined for the given constrainable and returns them in a list of constraint
	 * descriptors.
	 *
	 * @param constrainable The constrainable element (will be the executable for a method parameter).
	 * @param annotatedElement The annotated element. Usually the same as the constrainable except in the case of method
	 * parameters constraints when it is the parameter.
	 * @param kind The constraint location kind.
	 *
	 * @return A list of constraint descriptors for all constraint specified for the given member.
	 */
	private List<ConstraintDescriptorImpl<?>> findConstraints(Constrainable constrainable, JavaBeanAnnotatedElement annotatedElement,
			ConstraintLocationKind kind) {
		List<ConstraintDescriptorImpl<?>> metaData = newArrayList();
		for ( Annotation annotation : annotatedElement.getDeclaredAnnotations() ) {
			metaData.addAll( findConstraintAnnotations( constrainable, annotation, kind ) );
		}

		return metaData;
	}

	/**
	 * Finds all constraint annotations defined for the given constrainable and returns them in a list of constraint
	 * descriptors.
	 *
	 * @param constrainable The constrainable element (will be the executable for a method parameter).
	 * @param annotations The annotations.
	 * @param kind The constraint location kind.
	 *
	 * @return A list of constraint descriptors for all constraint specified for the given member.
	 */
	private List<ConstraintDescriptorImpl<?>> findConstraints(Constrainable constrainable, Annotation[] annotations,
			ConstraintLocationKind kind) {
		if ( annotations.length == 0 ) {
			return Collections.emptyList();
		}

		List<ConstraintDescriptorImpl<?>> metaData = newArrayList();
		for ( Annotation annotation : annotations ) {
			metaData.addAll( findConstraintAnnotations( constrainable, annotation, kind ) );
		}

		return metaData;
	}

	/**
	 * Examines the given annotation to see whether it is a single- or multi-valued constraint annotation.
	 *
	 * @param constrainable The constrainable to check for constraints annotations
	 * @param annotation The annotation to examine
	 * @param type the element type on which the annotation/constraint is placed on
	 * @param <A> the annotation type
	 *
	 * @return A list of constraint descriptors or the empty list in case {@code annotation} is neither a
	 * single nor multi-valued annotation.
	 */
	protected <A extends Annotation> List<ConstraintDescriptorImpl<?>> findConstraintAnnotations(
			Constrainable constrainable,
			A annotation,
			ConstraintLocationKind type) {

		// HV-1049 and HV-1311 - Ignore annotations from the JDK (jdk.internal.* and java.*); They cannot be constraint
		// annotations so skip them right here, as for the proper check we'd need package access permission for
		// "jdk.internal" and "java".
		if ( constraintCreationContext.getConstraintHelper().isJdkAnnotation( annotation.annotationType() ) ) {
			return Collections.emptyList();
		}

		List<Annotation> constraints = newArrayList();
		Class<? extends Annotation> annotationType = annotation.annotationType();
		if ( constraintCreationContext.getConstraintHelper().isConstraintAnnotation( annotationType ) ) {
			constraints.add( annotation );
		}
		else if ( constraintCreationContext.getConstraintHelper().isMultiValueConstraint( annotationType ) ) {
			constraints.addAll( constraintCreationContext.getConstraintHelper().getConstraintsFromMultiValueConstraint( annotation ) );
		}

		return constraints.stream()
				.map( c -> buildConstraintDescriptor( constrainable, c, type ) )
				.collect( Collectors.toList() );
	}

	private Map<Class<?>, Class<?>> getGroupConversions(AnnotatedType annotatedType) {
		return getGroupConversions(
				annotatedType.getAnnotation( ConvertGroup.class ),
				annotatedType.getAnnotation( ConvertGroup.List.class )
		);
	}

	private Map<Class<?>, Class<?>> getGroupConversions(ConvertGroup groupConversion, ConvertGroup.List groupConversionList) {
		if ( groupConversion == null && ( groupConversionList == null || groupConversionList.value().length == 0 ) ) {
			return Collections.emptyMap();
		}

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

	private <A extends Annotation> ConstraintDescriptorImpl<A> buildConstraintDescriptor(Constrainable constrainable,
			A annotation,
			ConstraintLocationKind type) {
		return new ConstraintDescriptorImpl<>(
				constraintCreationContext.getConstraintHelper(),
				constrainable,
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
	protected Set<MetaConstraint<?>> findTypeAnnotationConstraints(JavaBeanField javaBeanField) {
		return findTypeArgumentsConstraints(
				javaBeanField,
				new TypeArgumentFieldLocation( javaBeanField ),
				javaBeanField.getAnnotatedType()
		);
	}

	/**
	 * Finds type arguments constraints for method return values.
	 */
	protected Set<MetaConstraint<?>> findTypeAnnotationConstraints(JavaBeanExecutable<?> javaBeanExecutable) {
		return findTypeArgumentsConstraints(
				javaBeanExecutable,
				new TypeArgumentReturnValueLocation( javaBeanExecutable ),
				javaBeanExecutable.getAnnotatedType()
		);
	}

	private CascadingMetaDataBuilder findCascadingMetaData(JavaBeanParameter javaBeanParameter) {
		Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData = getTypeParametersCascadingMetadata( javaBeanParameter.getAnnotatedType(),
				javaBeanParameter.getTypeParameters() );

		try {
			return getCascadingMetaData( javaBeanParameter, containerElementTypesCascadingMetaData );
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			LOG.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
			return CascadingMetaDataBuilder.nonCascading();
		}
	}

	private CascadingMetaDataBuilder findCascadingMetaData(JavaBeanField javaBeanField) {
		Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData = getTypeParametersCascadingMetadata(
				javaBeanField.getAnnotatedType(),
				javaBeanField.getTypeParameters() );

		return getCascadingMetaData( javaBeanField, containerElementTypesCascadingMetaData );
	}

	private CascadingMetaDataBuilder findCascadingMetaData(JavaBeanExecutable<?> javaBeanExecutable) {
		Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData = getTypeParametersCascadingMetadata( javaBeanExecutable.getAnnotatedType(),
				javaBeanExecutable.getTypeParameters() );

		return getCascadingMetaData( javaBeanExecutable, containerElementTypesCascadingMetaData );
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
	 * @param javaBeanParameter the parameter
	 *
	 * @return a set of type arguments constraints, or an empty set if no constrained type arguments are found
	 */
	protected Set<MetaConstraint<?>> findTypeAnnotationConstraintsForExecutableParameter(JavaBeanExecutable<?> javaBeanExecutable,
			JavaBeanParameter javaBeanParameter) {
		try {
			return findTypeArgumentsConstraints(
					javaBeanExecutable,
					new TypeArgumentExecutableParameterLocation( javaBeanExecutable, javaBeanParameter.getIndex() ),
					javaBeanParameter.getAnnotatedType()
			);
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			LOG.warn( MESSAGES.constraintOnConstructorOfNonStaticInnerClass(), ex );
			return Collections.emptySet();
		}
	}

	private Set<MetaConstraint<?>> findTypeArgumentsConstraints(Constrainable constrainable, TypeArgumentLocation location, AnnotatedType annotatedType) {
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

			typeArgumentConstraints.addAll( findTypeUseConstraints( constrainable, annotatedArrayType, arrayElementTypeArgument, location, validatedType ) );

			typeArgumentConstraints.addAll( findTypeArgumentsConstraints( constrainable,
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

				typeArgumentConstraints.addAll( findTypeUseConstraints( constrainable, annotatedTypeParameter, typeVariable, location, validatedType ) );

				if ( validatedType instanceof ParameterizedType ) {
					typeArgumentConstraints.addAll( findTypeArgumentsConstraints( constrainable,
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
	private Set<MetaConstraint<?>> findTypeUseConstraints(Constrainable constrainable, AnnotatedType typeArgument, TypeVariable<?> typeVariable,
			TypeArgumentLocation location, Type type) {
		List<ConstraintDescriptorImpl<?>> constraintDescriptors = findConstraints( constrainable, typeArgument.getAnnotations(), ConstraintLocationKind.TYPE_USE );

		if ( constraintDescriptors.isEmpty() ) {
			return Collections.emptySet();
		}

		Set<MetaConstraint<?>> constraints = newHashSet( constraintDescriptors.size() );
		ConstraintLocation constraintLocation = ConstraintLocation.forTypeArgument( location.toConstraintLocation(), typeVariable, type );

		for ( ConstraintDescriptorImpl<?> constraintDescriptor : constraintDescriptors ) {
			constraints.add( MetaConstraints.create( constraintCreationContext.getTypeResolutionHelper(),
					constraintCreationContext.getValueExtractorManager(),
					constraintCreationContext.getConstraintValidatorManager(), constraintDescriptor,
					constraintLocation ) );
		}

		return constraints;
	}

	private CascadingMetaDataBuilder getCascadingMetaData(JavaBeanAnnotatedElement annotatedElement,
			Map<TypeVariable<?>, CascadingMetaDataBuilder> containerElementTypesCascadingMetaData) {
		return CascadingMetaDataBuilder.annotatedObject( annotatedElement.getType(), annotatedElement.isAnnotationPresent( Valid.class ),
				containerElementTypesCascadingMetaData, getGroupConversions( annotatedElement.getAnnotatedType() ) );
	}

	/**
	 * The location of a type argument before it is really considered a constraint location.
	 * <p>
	 * It avoids initializing a constraint location if we did not find any constraints. This is especially useful in
	 * a Java 9 environment as {@link ConstraintLocation#forField(org.hibernate.validator.internal.properties.Field)}
	 * or {@link ConstraintLocation#forGetter(Getter)} tries to make the {@code Member} accessible
	 * which might not be possible (for instance for {@code java.util} classes).
	 */
	private interface TypeArgumentLocation {
		ConstraintLocation toConstraintLocation();
	}

	private static class TypeArgumentExecutableParameterLocation implements TypeArgumentLocation {
		private final JavaBeanExecutable<?> javaBeanExecutable;

		private final int index;

		private TypeArgumentExecutableParameterLocation(JavaBeanExecutable<?> javaBeanExecutable, int index) {
			this.javaBeanExecutable = javaBeanExecutable;
			this.index = index;
		}

		@Override
		public ConstraintLocation toConstraintLocation() {
			return ConstraintLocation.forParameter( javaBeanExecutable, index );
		}
	}

	private static class TypeArgumentFieldLocation implements TypeArgumentLocation {
		private final JavaBeanField javaBeanField;

		private TypeArgumentFieldLocation(JavaBeanField javaBeanField) {
			this.javaBeanField = javaBeanField;
		}

		@Override
		public ConstraintLocation toConstraintLocation() {
			return ConstraintLocation.forField( javaBeanField );
		}
	}

	private static class TypeArgumentReturnValueLocation implements TypeArgumentLocation {
		private final JavaBeanExecutable<?> javaBeanExecutable;

		private TypeArgumentReturnValueLocation(JavaBeanExecutable<?> javaBeanExecutable) {
			this.javaBeanExecutable = javaBeanExecutable;
		}

		@Override
		public ConstraintLocation toConstraintLocation() {
			return ConstraintLocation.forReturnValue( javaBeanExecutable );
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
