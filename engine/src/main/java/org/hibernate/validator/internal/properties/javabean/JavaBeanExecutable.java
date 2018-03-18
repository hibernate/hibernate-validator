/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.properties.Callable;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.TypeHelper;

/**
 * @author Marko Bekhta
 */
public class JavaBeanExecutable implements Callable {

	protected final Executable executable;
	private final Type typeForValidatorResolution;
	private final String name;
	private final boolean hasParameters;
	private final boolean hasReturnValue;
	private final Type type;

	JavaBeanExecutable(Executable executable) {
		this.executable = executable;
		this.name = executable.getName();
		this.type = ReflectionHelper.typeOf( executable );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( type );
		this.hasParameters = executable.getParameterTypes().length > 0;
		this.hasReturnValue = hasReturnValue( executable );
	}

	@Override
	public boolean hasReturnValue() {
		return hasReturnValue;
	}

	@Override
	public boolean hasParameters() {
		return hasParameters;
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
	public Class<?>[] getParameterTypes() {
		return executable.getParameterTypes();
	}

	@Override
	public Type[] getGenericParameterTypes() {
		return executable.getGenericParameterTypes();
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
	public boolean isConstructor() {
		return executable instanceof Constructor;
	}

	@Override
	public String getSignature() {
		return ExecutableHelper.getSignature( executable );
	}

	@Override
	public Type getTypeOfParameter(int parameterIndex) {
		Type[] genericParameterTypes = executable.getGenericParameterTypes();

		// getGenericParameterTypes() doesn't return synthetic parameters; in this case fall back to getParameterTypes()
		if ( parameterIndex >= genericParameterTypes.length ) {
			genericParameterTypes = executable.getParameterTypes();
		}

		Type type = genericParameterTypes[parameterIndex];

		if ( type instanceof TypeVariable ) {
			type = TypeHelper.getErasedType( type );
		}
		return type;
	}

	@Override
	public boolean overrides(ExecutableHelper executableHelper, Callable superTypeMethod) {
		return executableHelper.overrides( ( (Method) this.executable ), ( (Method) ( (JavaBeanExecutable) superTypeMethod ).executable ) );
	}

	@Override
	public boolean isResolvedToSameMethodInHierarchy(ExecutableHelper executableHelper, Class<?> mainSubType, Callable superTypeMethod) {
		return executableHelper.isResolvedToSameMethodInHierarchy( mainSubType, ( (Method) this.executable ), ( (Method) ( (JavaBeanExecutable) superTypeMethod ).executable ) );
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || this.getClass() != o.getClass() ) {
			return false;
		}

		JavaBeanExecutable that = (JavaBeanExecutable) o;

		if ( this.hasParameters != that.hasParameters ) {
			return false;
		}
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
		result = 31 * result + ( this.hasParameters ? 1 : 0 );
		result = 31 * result + ( this.hasReturnValue ? 1 : 0 );
		result = 31 * result + this.type.hashCode();
		return result;
	}

	private boolean hasReturnValue(Executable executable) {
		if ( executable instanceof Constructor ) {
			return true;
		}
		else {
			return ( (Method) executable ).getGenericReturnType() != void.class;
		}
	}
}
