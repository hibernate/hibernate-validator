/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.jandex;

import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * Builder for constrained methods that uses Jandex index.
 *
 * @author Marko Bekhta
 */
public class ConstrainedMethodJandexBuilder extends AbstractConstrainedElementJandexBuilder {

	protected final ExecutableParameterNameProvider parameterNameProvider;

	public ConstrainedMethodJandexBuilder(ConstraintHelper constraintHelper, JandexHelper jandexHelper,
			AnnotationProcessingOptions annotationProcessingOptions, ExecutableParameterNameProvider parameterNameProvider,
			List<DotName> constraintAnnotations) {
		super( constraintHelper, jandexHelper, annotationProcessingOptions, constraintAnnotations );
		this.parameterNameProvider = parameterNameProvider;
	}

	/**
	 * Gets {@link ConstrainedExecutable}s from a given class.
	 *
	 * @param classInfo a class in which to look for constrained fields
	 * @param beanClass same class as {@code classInfo} but represented as {@link Class}
	 *
	 * @return a stream of {@link ConstrainedElement}s that represents fields
	 */
	public Stream<ConstrainedElement> getConstrainedExecutables(ClassInfo classInfo, Class<?> beanClass) {
		// HV-172; ignoring synthetic methods (inserted by the compiler), as they can't have any constraints
		// anyway and possibly hide the actual method with the same signature in the built meta model
		//TODO: check for synthetic somehow ?
		// void <init>() - such method is included in the method list but how to filter it out ?

		return classInfo.methods().stream()
				.filter( methodInfo -> !Modifier.isStatic( methodInfo.flags() ) /*&& !Modifier.isSynthetic( methodInfo.flags() )*/ )
				.map( methodInfo -> toConstrainedExecutable( beanClass, methodInfo ) );
	}

	/**
	 * Converts given method to {@link ConstrainedExecutable}.
	 *
	 * @param beanClass a {@link Class} where {@code methodInfo} is located
	 * @param methodInfo a method to convert
	 *
	 * @return {@link ConstrainedExecutable} representation of a given method
	 */
	private ConstrainedExecutable toConstrainedExecutable(Class<?> beanClass, MethodInfo methodInfo) {
		Executable executable = null;
		//TODO: this try/catch should be removed once filtering of methods is fixed.
		try {
			executable = findExecutable( beanClass, methodInfo );
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
			return null;
		}
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
				// TODO Jandex: we need to determine the TypeVariables
				Collections.emptyList(),
				commonInformation.getUnwrapMode()
		);
	}

	/**
	 * Provides a stream of parameter constraints for a given method.
	 *
	 * @param beanClass a hosting class of a given method
	 * @param executable an executable member that represents {@code methodInfo}
	 * @param methodInfo method to retrieve parameters from
	 *
	 * @return a {@link Stream} of {@link ConstrainedParameter} for a given method
	 */
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

	/**
	 * Converts given parameter information to {@link ConstrainedParameter}.
	 *
	 * @param parameterInformation {@link ParameterInformation} containing parameter information
	 * @param executable represents a method of interest
	 *
	 * @return an instance of {@link ConstrainedParameter} based on input parameters
	 */
	private ConstrainedParameter toConstrainedParameter(ParameterInformation parameterInformation, Executable executable) {
		CommonConstraintInformation commonInformation;
		Stream<MetaConstraint<?>> parameterConstraints;
		Set<MetaConstraint<?>> typeArgumentsConstraints;
		Class<?> parameterType = jandexHelper.getClassForName( parameterInformation.getType().name().toString() );

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
				// TODO Jandex: we need to determine the TypeVariables
				Collections.emptyList(),
				commonInformation.getUnwrapMode()
		);
	}

	/**
	 * Converts {@link ConstraintDescriptorImpl} to {@link MetaConstraint}.
	 *
	 * @param annotationInstances collection of annotations declared on an executable
	 * @param executable an executable under investigation
	 *
	 * @return a stream of {@link MetaConstraint}s for a given executable
	 */
	private Stream<MetaConstraint<?>> findMetaConstraints(Collection<AnnotationInstance> annotationInstances, Executable executable) {
		return findConstraints( annotationInstances, executable )
				.map( descriptor -> createMetaConstraint( executable, descriptor ) );
	}

	/**
	 * Converts {@link ConstraintDescriptorImpl} to {@link MetaConstraint}.
	 *
	 * @param parameterInformation parameter information
	 * @param executable an executable under investigation
	 *
	 * @return a stream of {@link MetaConstraint}s for a given parameter executable
	 */
	private Stream<MetaConstraint<?>> findMetaConstraints(ParameterInformation parameterInformation, Executable executable) {
		return findConstraints( parameterInformation.getType().annotations(), executable )
				.map( descriptor -> createMetaConstraint( executable, parameterInformation.getIndex(), descriptor ) );
	}

	/**
	 * Find an {@link Executable} by given bean class and method information.
	 *
	 * @param beanClass a bean class in which to look for the executable
	 * @param methodInfo {@link MethodInfo} representing information about the executable
	 *
	 * @return a {@link Executable} for the given information
	 *
	 * @throws IllegalArgumentException if no executable was found for a given bean class and method information
	 */
	private Executable findExecutable(Class<?> beanClass, MethodInfo methodInfo) {
		try {
			if ( "<init>".equals( methodInfo.name() ) ) {
				// it means it is a constructor:
				return beanClass.getDeclaredConstructor( methodInfo.parameters().stream()
						.map( type -> jandexHelper.getClassForName( type.name().toString() ) )
						.toArray( size -> new Class<?>[size] ) );
			}
			else {
				return beanClass.getDeclaredMethod(
						methodInfo.name(),
						methodInfo.parameters().stream()
								.map( type -> jandexHelper.getClassForName( type.name().toString() ) )
								.toArray( size -> new Class<?>[size] )
				);
			}
		}
		catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
					String.format( "Wasn't able to find a executable for a given parameters. Executable name - %s in bean - %s", methodInfo.name(),
							beanClass.getName()
					),
					e
			);
		}
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
