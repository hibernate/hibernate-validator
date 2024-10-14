/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.properties.Signature;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public abstract class JavaBeanExecutable<T extends Executable> implements Callable, JavaBeanAnnotatedConstrainable {

	protected final T executable;
	private final Type typeForValidatorResolution;
	private final boolean hasReturnValue;
	private final Type type;
	private final List<JavaBeanParameter> parameters;

	JavaBeanExecutable(T executable, boolean hasReturnValue) {
		this.executable = executable;
		this.type = ReflectionHelper.typeOf( executable );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( type );
		this.hasReturnValue = hasReturnValue;
		this.parameters = getParameters( executable );
	}

	@Override
	public boolean hasReturnValue() {
		return hasReturnValue;
	}

	@Override
	public boolean hasParameters() {
		return !parameters.isEmpty();
	}

	@Override
	public String getName() {
		return executable.getName();
	}

	@Override
	public Class<?> getDeclaringClass() {
		return executable.getDeclaringClass();
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getParameterName(ExecutableParameterNameProvider parameterNameProvider, int parameterIndex) {
		return parameterNameProvider.getParameterNames( executable ).get( parameterIndex );
	}

	@Override
	public boolean isPrivate() {
		return Modifier.isPrivate( executable.getModifiers() );
	}

	@Override
	public Signature getSignature() {
		return ExecutableHelper.getSignature( executable );
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return executable.getDeclaredAnnotations();
	}

	@Override
	public boolean overrides(ExecutableHelper executableHelper, Callable superTypeMethod) {
		return executableHelper.overrides( ( (Method) this.executable ), ( (Method) ( (JavaBeanExecutable<?>) superTypeMethod ).executable ) );
	}

	@Override
	public boolean isResolvedToSameMethodInHierarchy(ExecutableHelper executableHelper, Class<?> mainSubType, Callable superTypeMethod) {
		return executableHelper.isResolvedToSameMethodInHierarchy( mainSubType, ( (Method) this.executable ), ( (Method) ( (JavaBeanExecutable<?>) superTypeMethod ).executable ) );
	}

	@Override
	public Type getGenericType() {
		return ReflectionHelper.typeOf( executable );
	}

	@Override
	public AnnotatedType getAnnotatedType() {
		return executable.getAnnotatedReturnType();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return executable.getAnnotation( annotationClass );
	}

	public List<JavaBeanParameter> getParameters() {
		return parameters;
	}

	@Override
	public Type getParameterGenericType(int index) {
		return parameters.get( index ).getGenericType();
	}

	@Override
	public int getParameterCount() {
		return parameters.size();
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return executable.getParameterTypes();
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || this.getClass() != o.getClass() ) {
			return false;
		}

		JavaBeanExecutable<?> that = (JavaBeanExecutable<?>) o;

		if ( this.hasReturnValue != that.hasReturnValue ) {
			return false;
		}
		if ( !this.executable.equals( that.executable ) ) {
			return false;
		}
		if ( !this.typeForValidatorResolution.equals( that.typeForValidatorResolution ) ) {
			return false;
		}
		return this.type.equals( that.type );
	}

	@Override
	public int hashCode() {
		int result = this.executable.hashCode();
		result = 31 * result + this.typeForValidatorResolution.hashCode();
		result = 31 * result + ( this.hasReturnValue ? 1 : 0 );
		result = 31 * result + this.type.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return ExecutableHelper.getExecutableAsString(
				getDeclaringClass().getSimpleName() + "#" + executable.getName(),
				executable.getParameterTypes()
		);
	}

	private static List<JavaBeanParameter> getParameters(Executable executable) {
		if ( executable.getParameterCount() == 0 ) {
			return Collections.emptyList();
		}

		int parameterCount = executable.getParameterCount();
		List<JavaBeanParameter> parameters = new ArrayList<>( parameterCount );

		Parameter[] parameterArray = executable.getParameters();
		Class<?>[] parameterTypes = executable.getParameterTypes();
		AnnotatedType[] annotatedTypes = executable.getAnnotatedParameterTypes();

		Annotation[][] parameterAnnotationsArray = executable.getParameterAnnotations();
		Annotation[][] annotationsForJDK8303112 =
				recomputeParameterAnnotationsForJDK8303112( parameterArray, parameterAnnotationsArray );

		for ( int i = 0; i < parameterArray.length; i++ ) {
			parameters.add( new JavaBeanParameter( i, parameterArray[i], parameterTypes[i], annotatedTypes[i],
					annotationsForJDK8303112 != null ? annotationsForJDK8303112[i] : null ) );
		}

		return CollectionHelper.toImmutableList( parameters );
	}

	/**
	 * This is a workaround for <a href="https://bugs.openjdk.org/browse/JDK-8303112">JDK-8303112</a>.
	 * @param parameters The result of calling {@link Executable#getParameters()}
	 * @param parameterAnnotationsArray The result of calling  {@link Executable#getParameterAnnotations()}
	 * @return A fixed version of {@code parameterAnnotationsArray},
	 * or {@code null} if {@code parameterAnnotationsArray} is fine an unaffected by JDK-8303112.
	 */
	private static Annotation[][] recomputeParameterAnnotationsForJDK8303112(Parameter[] parameters,
			Annotation[][] parameterAnnotationsArray) {
		int parameterCount = parameters.length;
		if ( parameterAnnotationsArray.length == parameterCount ) {
			// Not affected by JDK-8303112
			return null;
		}

		// We're in a situation where parameter.getAnnotation()/parameter.getAnnotations()
		// is buggy when there are implicit/synthetic parameters,
		// because constructor.getParameterAnnotations() (wrongly) ignores implicit/synthetic parameters
		// while parameter.getAnnotations() (rightly) assumes they are present in the array.

		Annotation[][] annotationsForJDK8303112;
		annotationsForJDK8303112 = new Annotation[parameterCount][];
		int nonImplicitNorSyntheticParamIndex = 0;
		for ( int i = 0; i < parameterCount; i++ ) {
			Parameter parameter = parameters[i];
			if ( parameter.isImplicit() || parameter.isSynthetic() ) {
				annotationsForJDK8303112[i] = new Annotation[0];
			}
			else if ( nonImplicitNorSyntheticParamIndex < parameterAnnotationsArray.length ) {
				annotationsForJDK8303112[i] =
						parameterAnnotationsArray[nonImplicitNorSyntheticParamIndex];
				++nonImplicitNorSyntheticParamIndex;
			}
			else {
				// Something is wrong; most likely the class wasn't compiled with -parameters
				// and so isImplicit/isSynthetic always return false.
				// As a last resort, assume the implicit/synthetic parameters are the first ones.
				nonImplicitNorSyntheticParamIndex = parameterCount - parameterAnnotationsArray.length;
				Arrays.fill( annotationsForJDK8303112, 0, nonImplicitNorSyntheticParamIndex,
						new Annotation[0] );
				System.arraycopy( parameterAnnotationsArray, 0, annotationsForJDK8303112,
						nonImplicitNorSyntheticParamIndex, parameterAnnotationsArray.length );
				return annotationsForJDK8303112;
			}
		}
		return annotationsForJDK8303112;
	}
}
