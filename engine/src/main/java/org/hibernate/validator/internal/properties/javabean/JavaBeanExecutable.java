/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public abstract class JavaBeanExecutable<T extends Executable> implements Callable, JavaBeanAnnotatedConstrainable {

	protected final T executable;
	private final Type typeForValidatorResolution;
	private final String name;
	private final boolean hasReturnValue;
	private final Type type;
	private final List<JavaBeanParameter> parameters;

	JavaBeanExecutable(T executable, boolean hasReturnValue) {
		this.executable = executable;
		this.name = executable.getName();
		this.type = ReflectionHelper.typeOf( executable );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( type );
		this.hasReturnValue = hasReturnValue;

		if ( executable.getParameterCount() > 0 ) {
			List<JavaBeanParameter> parameters = new ArrayList<>( executable.getParameterCount() );

			Parameter[] parameterArray = executable.getParameters();
			Class<?>[] parameterTypes = executable.getParameterTypes();
			Type[] genericParameterTypes = executable.getGenericParameterTypes();

			for ( int i = 0; i < parameterArray.length; i++ ) {
				parameters.add( new JavaBeanParameter( this, i, parameterArray[i], parameterTypes[i],
						getParameterGenericType( parameterTypes, genericParameterTypes, i ) ) );
			}
			this.parameters = CollectionHelper.toImmutableList( parameters );
		}
		else {
			this.parameters = Collections.emptyList();
		}
	}

	public static JavaBeanExecutable<?> of(Executable executable) {
		if ( executable instanceof Constructor ) {
			return new JavaBeanConstructor( (Constructor<?>) executable );
		}

		return of( ( (Method) executable ) );
	}

	public static JavaBeanMethod of(Method method) {
		if ( ReflectionHelper.isGetterMethod( method ) ) {
			return new JavaBeanGetter( method );
		}

		return new JavaBeanMethod( method );
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
		return name;
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
	public String getSignature() {
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
		if ( !this.name.equals( that.name ) ) {
			return false;
		}
		return this.type.equals( that.type );
	}

	@Override
	public int hashCode() {
		int result = this.executable.hashCode();
		result = 31 * result + this.typeForValidatorResolution.hashCode();
		result = 31 * result + this.name.hashCode();
		result = 31 * result + ( this.hasReturnValue ? 1 : 0 );
		result = 31 * result + this.type.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return ExecutableHelper.getExecutableAsString(
				getDeclaringClass().getSimpleName() + "#" + name,
				executable.getParameterTypes()
		);
	}

	private static Type getParameterGenericType(Type[] parameterTypes, Type[] genericParameterTypes, int parameterIndex) {
		// getGenericParameterTypes() doesn't return synthetic parameters; in this case fall back to getParameterTypes()
		Type[] typesToConsider;
		if ( parameterIndex >= genericParameterTypes.length ) {
			typesToConsider = parameterTypes;
		}
		else {
			typesToConsider = genericParameterTypes;
		}

		Type type = typesToConsider[parameterIndex];

		if ( type instanceof TypeVariable ) {
			type = TypeHelper.getErasedType( type );
		}
		return type;
	}
}
