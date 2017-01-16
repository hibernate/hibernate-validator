/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.Valid;

import org.hibernate.validator.internal.engine.cascading.AnnotatedObject;
import org.hibernate.validator.internal.engine.cascading.ArrayElement;
import org.hibernate.validator.internal.metadata.core.AnnotationProcessingOptions;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.core.MetaConstraint;
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl;
import org.hibernate.validator.internal.metadata.jandex.util.JandexHelper;
import org.hibernate.validator.internal.metadata.location.ConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedParameter;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

/**
 * Builder used to extract builder and method constraints from the Jandex index.
 *
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public class ConstrainedMethodJandexBuilder extends AbstractConstrainedElementJandexBuilder {

	private static final int SYNTHETIC = 0x1000;
	private static final int BRIDGE = 0x0040;

	protected final ExecutableParameterNameProvider parameterNameProvider;

	public ConstrainedMethodJandexBuilder(ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			AnnotationProcessingOptions annotationProcessingOptions, ExecutableParameterNameProvider parameterNameProvider,
			List<DotName> constraintAnnotations) {
		super( constraintHelper, jandexHelper, annotationProcessingOptions, constraintAnnotations );
		this.parameterNameProvider = parameterNameProvider;
	}

	public Stream<ConstrainedElement> getConstrainedExecutables(ClassInfo classInfo, Class<?> beanClass) {
		// HV-172; ignoring synthetic methods (inserted by the compiler), as they can't have any constraints
		// anyway and possibly hide the actual method with the same signature in the built meta model

		return classInfo.methods().stream()
				.filter( methodInfo -> !Modifier.isStatic( methodInfo.flags() ) && !isSynthetic( methodInfo ) )
				.map( methodInfo -> toConstrainedExecutable( beanClass, methodInfo ) );
	}

	private ConstrainedExecutable toConstrainedExecutable(Class<?> beanClass, MethodInfo methodInfo) {
		Executable executable = findExecutable( beanClass, methodInfo );

		Stream<ConstrainedParameter> parameterConstraints = getParameterMetaData( beanClass, executable, methodInfo );

		Set<MetaConstraint<?>> crossParameterConstraints;
		Map<ConstraintDescriptorImpl.ConstraintType, Set<MetaConstraint<?>>> executableConstraints =
				findMetaConstraints( methodInfo.annotations(), executable )
						.collect( Collectors.groupingBy( constraint -> constraint.getDescriptor().getConstraintType(), Collectors.toSet() ) );

		if ( annotationProcessingOptions.areCrossParameterConstraintsIgnoredFor( executable ) ) {
			crossParameterConstraints = Collections.emptySet();
		}
		else {
			crossParameterConstraints = executableConstraints.containsKey( ConstraintDescriptorImpl.ConstraintType.CROSS_PARAMETER ) ?
					executableConstraints.get( ConstraintDescriptorImpl.ConstraintType.CROSS_PARAMETER ) :
					Collections.emptySet();
		}

		Set<MetaConstraint<?>> returnValueConstraints;
		Set<MetaConstraint<?>> typeArgumentsConstraints;
		CommonConstraintInformation commonInformation;
		if ( annotationProcessingOptions.areReturnValueConstraintsIgnoredFor( executable ) || Type.Kind.VOID.equals( methodInfo.returnType().kind() ) ) {
			returnValueConstraints = Collections.emptySet();
			typeArgumentsConstraints = Collections.emptySet();
			commonInformation = new CommonConstraintInformation();
		}
		else {
			// check for any constraints on return type only if it is not void
			boolean isCascading = jandexHelper.isCascading( methodInfo.annotations() );
			typeArgumentsConstraints = findTypeAnnotationConstraintsForMember(
					new MemberInformation(
							methodInfo.returnType(),
							methodInfo.name(),
							executable,
							ConstraintLocation.forReturnValue( executable ),
							beanClass
					),
					isCascading
			).collect( Collectors.toSet() );

			commonInformation = findCommonConstraintInformation(
					methodInfo.returnType(),
					methodInfo.annotations(),
					!typeArgumentsConstraints.isEmpty(),
					isCascading
			);

			returnValueConstraints = executableConstraints.containsKey( ConstraintDescriptorImpl.ConstraintType.GENERIC ) ?
					executableConstraints.get( ConstraintDescriptorImpl.ConstraintType.GENERIC ) :
					Collections.emptySet();
		}

		return new ConstrainedExecutable(
				ConfigurationSource.JANDEX,
				executable,
				parameterConstraints.collect( Collectors.toList() ),
				crossParameterConstraints,
				returnValueConstraints,
				typeArgumentsConstraints,
				commonInformation.getGroupConversions(),
				findCascadingTypeParameters( executable ),
				commonInformation.getUnwrapMode()
		);
	}

	private Stream<ConstrainedParameter> getParameterMetaData(Class<?> beanClass, Executable executable, MethodInfo methodInfo) {
		if ( methodInfo.parameters().isEmpty() ) {
			return Stream.empty();
		}

		//TODO: maybe parameter name provide can be changed and not use executable ?
		List<String> parameterNames = parameterNameProvider.getParameterNames( executable );

		List<ParameterInformation> parameters = CollectionHelper.newArrayList();
		for ( int i = 0; i < parameterNames.size(); i++ ) {
			parameters.add( new ParameterInformation( parameterNames.get( i ), i, methodInfo.parameters().get( i ), beanClass ) );
		}

		return parameters.stream()
				.map( parameterInformation -> toConstrainedParameter( parameterInformation, executable ) );
	}

	private ConstrainedParameter toConstrainedParameter(ParameterInformation parameterInformation, Executable executable) {
			CommonConstraintInformation commonInformation;
			Stream<MetaConstraint<?>> parameterConstraints;
			Set<MetaConstraint<?>> typeArgumentsConstraints;
			Class<?> parameterType = jandexHelper.getClassForName( parameterInformation.getType().name() );

		if ( annotationProcessingOptions.areParameterConstraintsIgnoredFor( executable, parameterInformation.getIndex() ) ) {
			parameterConstraints = Stream.empty();
			typeArgumentsConstraints = Collections.emptySet();
			commonInformation = new CommonConstraintInformation();
		}
		else {
			boolean isCascading = jandexHelper.isCascading( parameterInformation.getType().annotations() );
			typeArgumentsConstraints = findTypeAnnotationConstraintsForMember(
					new MemberInformation(
							parameterInformation.getType(),
							parameterInformation.getName(),
							executable,
							ConstraintLocation.forParameter( executable, parameterInformation.getIndex() ),
							parameterInformation.getBeanClass()
					),
					isCascading
			).collect( Collectors.toSet() );

			commonInformation = findCommonConstraintInformation(
					parameterInformation.getType(),
					parameterInformation.getType().annotations(),
					!typeArgumentsConstraints.isEmpty(),
					isCascading
			);

			parameterConstraints = findMetaConstraints( parameterInformation, executable );
		}

		return new ConstrainedParameter(
				ConfigurationSource.JANDEX,
				executable,
				parameterType,
				parameterInformation.getIndex(),
				parameterInformation.getName(),
				parameterConstraints.collect( Collectors.toSet() ),
				typeArgumentsConstraints,
				commonInformation.getGroupConversions(),
				findCascadingTypeParameters( executable ),
				commonInformation.getUnwrapMode()
		);
	}

	private Stream<MetaConstraint<?>> findMetaConstraints(Collection<AnnotationInstance> annotationInstances, Executable executable) {
		return findConstraints( annotationInstances, executable )
				.map( descriptor -> createMetaConstraint( executable, descriptor ) );
	}

	private Stream<MetaConstraint<?>> findMetaConstraints(ParameterInformation parameterInformation, Executable executable) {
		return findConstraints( parameterInformation.getType().annotations(), executable )
				.map( descriptor -> createMetaConstraint( executable, parameterInformation.getIndex(), descriptor ) );
	}

	private Executable findExecutable(Class<?> beanClass, MethodInfo methodInfo) {
		try {
			if ( isConstructor( methodInfo ) ) {
				return beanClass.getDeclaredConstructor( methodInfo.parameters().stream()
						.map( type -> jandexHelper.getClassForName( type.name() ) )
						.toArray( size -> new Class<?>[size] ) );
			}
			else {
				return beanClass.getDeclaredMethod(
						methodInfo.name(),
						methodInfo.parameters().stream()
								.map( type -> jandexHelper.getClassForName( type.name() ) )
								.toArray( size -> new Class<?>[size] )
				);
			}
		}
		catch (NoSuchMethodException e) {
			// TODO add the parameter information to the log. It would probably be nice if Jandex could expose a method for that.
			throw LOG.getUnableToFindMethodReferencedInJandexIndex( beanClass, methodInfo.name(), e );
		}
	}

	private <A extends Annotation> MetaConstraint<A> createMetaConstraint(Executable member, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<>(
				descriptor,
				ConstraintDescriptorImpl.ConstraintType.GENERIC.equals( descriptor.getConstraintType() ) ?
						ConstraintLocation.forReturnValue( member ) : ConstraintLocation.forCrossParameter( member )
		);
	}

	private <A extends Annotation> MetaConstraint<A> createMetaConstraint(Executable member, int parameterIndex, ConstraintDescriptorImpl<A> descriptor) {
		return new MetaConstraint<>(
				descriptor,
				ConstraintLocation.forParameter( member, parameterIndex )
		);
	}

	private boolean isConstructor(MethodInfo methodInfo) {
		// currently, Jandex does not expose this in a practical way
		return "<init>".equals( methodInfo.name() );
	}

	private boolean isSynthetic(MethodInfo methodInfo) {
		// currently, Jandex does not expose this in a practical way
		return ( methodInfo.flags() & ( SYNTHETIC | BRIDGE ) ) != 0;
	}

	/**
	 * TODO: this method was directly copied from AnnotationMetaDataProvider, we will need to refactor this but it's
	 * better to wait for the dust to settle a bit
	 */
	private List<TypeVariable<?>> findCascadingTypeParameters(Executable executable) {
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

		List<TypeVariable<?>> cascadingTypeParameters = getCascadingTypeParameters( typeParameters, annotatedType );

		if ( executable.isAnnotationPresent( Valid.class ) ) {
			cascadingTypeParameters.add( isArray ? ArrayElement.INSTANCE : AnnotatedObject.INSTANCE );
		}

		return cascadingTypeParameters;
	}

	/**
	 * Simple POJO that contains {@link Type}, name and index of a method parameter.
	 */
	private static class ParameterInformation {

		private String name;
		private int index;
		private Type type;
		private Class<?> beanClass;

		public ParameterInformation(String name, int index, Type type, Class<?> beanClass) {
			this.name = name;
			this.index = index;
			this.type = type;
			this.beanClass = beanClass;
		}

		public String getName() {
			return name;
		}

		public int getIndex() {
			return index;
		}

		public Type getType() {
			return type;
		}

		public Class<?> getBeanClass() {
			return beanClass;
		}
	}
}
