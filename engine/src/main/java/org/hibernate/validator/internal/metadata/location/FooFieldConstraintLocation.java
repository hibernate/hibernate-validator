/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;

import org.hibernate.validator.internal.Foo;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.StringHelper;

/**
 * Field constraint location.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class FooFieldConstraintLocation implements ConstraintLocation {

	/**
	 * The member the constraint was defined on.
	 */
	private final MethodHandle property;
	private final Field string;


	/**
	 * The property name associated with the member.
	 */
	private final String propertyName;

	/**
	 * The type to be used for validator resolution for constraints at this location.
	 */
	private final Type typeForValidatorResolution;

	FooFieldConstraintLocation() {
		this.propertyName = "string";
		this.typeForValidatorResolution = String.class;
		try {
			string = Foo.class.getDeclaredField( "string" );
			string.setAccessible( true );
			property = MethodHandles.lookup().unreflectGetter( string );
		}
		catch (IllegalAccessException | NoSuchFieldException e) {
			throw new IllegalStateException( e );
		}
	}

	@Override
	public Class<?> getDeclaringClass() {
		return Foo.class;
	}

	@Override
	public Member getMember() {
		return string;
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
		return "FieldConstraintLocation [member=" + StringHelper.toShortString( string ) + ", typeForValidatorResolution="
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

		FooFieldConstraintLocation that = (FooFieldConstraintLocation) o;

		if ( string != null ? !string.equals( that.string ) : that.string != null ) {
			return false;
		}
		if ( !typeForValidatorResolution.equals( that.typeForValidatorResolution ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = string != null ? string.hashCode() : 0;
		result = 31 * result + typeForValidatorResolution.hashCode();
		return result;
	}

}
