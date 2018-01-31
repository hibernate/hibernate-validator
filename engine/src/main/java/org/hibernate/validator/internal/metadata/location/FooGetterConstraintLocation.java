/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.Foo;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.StringHelper;

/**
 * Getter method constraint location.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class FooGetterConstraintLocation implements ConstraintLocation {

	/**
	 * The method the constraint was defined on.
	 */
	private static final MethodHandle property;
	private static final Method method;

	static {
		try {
			method = Foo.class.getMethod( "isTrue" );
			method.setAccessible( true );
			property = MethodHandles.lookup().unreflect( method );
		}
		catch (IllegalAccessException | NoSuchMethodException e) {
			throw new IllegalStateException( e );
		}
	}

	/**
	 * The property name associated with the method.
	 */
	private final String propertyName;

	/**
	 * The type to be used for validator resolution for constraints at this location.
	 */
	private final Type typeForValidatorResolution;

	FooGetterConstraintLocation() {
		this.propertyName = ReflectionHelper.getPropertyName( method );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( ReflectionHelper.typeOf( method ) );
	}

	@Override
	public Class<?> getDeclaringClass() {
		return method.getDeclaringClass();
	}

	@Override
	public Method getMember() {
		return method;
	}

	public String getPropertyName() {
		return propertyName;
	}

	@Override
	public Type getTypeForValidatorResolution() {
		return typeForValidatorResolution;
	}

	@Override
	public void appendTo(ExecutableParameterNameProvider parameterNameProvider, PathImpl path) {
		path.addPropertyNode( propertyName );
	}

	@Override
	public Object getValue(Object parent) {
		try {
			return property.invoke( parent );
		}
		catch (Throwable throwable) {
			throw new IllegalStateException( throwable );
		}
	}

	@Override
	public String toString() {
		return "GetterConstraintLocation [method=" + StringHelper.toShortString( method ) + ", typeForValidatorResolution="
				+ StringHelper.toShortString( typeForValidatorResolution ) + "]";
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}

		FooGetterConstraintLocation that = (FooGetterConstraintLocation) o;

		if ( method != null ? !method.equals( that.method ) : that.method != null ) {
			return false;
		}
		if ( !typeForValidatorResolution.equals( that.typeForValidatorResolution ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = method.hashCode();
		result = 31 * result + typeForValidatorResolution.hashCode();
		return result;
	}

}
