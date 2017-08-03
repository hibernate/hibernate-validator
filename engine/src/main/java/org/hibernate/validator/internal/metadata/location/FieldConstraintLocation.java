/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.HibernateValidatorPermission;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;

/**
 * Field constraint location.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class FieldConstraintLocation implements ConstraintLocation {

	/**
	 * The member the constraint was defined on.
	 */
	private final Field field;

	private final Field accessibleField;

	/**
	 * The property name associated with the member.
	 */
	private final String propertyName;

	/**
	 * The type to be used for validator resolution for constraints at this location.
	 */
	private final Type typeForValidatorResolution;


	FieldConstraintLocation(Field field) {
		this.field = field;
		this.accessibleField = getAccessible( field );
		this.propertyName = ReflectionHelper.getPropertyName( field );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( ReflectionHelper.typeOf( field ) );
	}

	@Override
	public Class<?> getDeclaringClass() {
		return field.getDeclaringClass();
	}

	@Override
	public Member getMember() {
		return field;
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
		return ReflectionHelper.getValue( accessibleField, parent );
	}

	@Override
	public String toString() {
		return "FieldConstraintLocation [member=" + StringHelper.toShortString( field ) + ", typeForValidatorResolution="
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

		FieldConstraintLocation that = (FieldConstraintLocation) o;

		if ( field != null ? !field.equals( that.field ) : that.field != null ) {
			return false;
		}
		if ( !typeForValidatorResolution.equals( that.typeForValidatorResolution ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = field != null ? field.hashCode() : 0;
		result = 31 * result + typeForValidatorResolution.hashCode();
		return result;
	}

	/**
	 * Returns an accessible version of the given member. Will be the given member itself in case it is accessible,
	 * otherwise a copy which is set accessible.
	 */
	private static Field getAccessible(Field original) {
		if ( original.isAccessible() ) {
			return original;
		}

		SecurityManager sm = System.getSecurityManager();
		if ( sm != null ) {
			sm.checkPermission( HibernateValidatorPermission.ACCESS_PRIVATE_MEMBERS );
		}

		Class<?> clazz = original.getDeclaringClass();

		return run( GetDeclaredField.andMakeAccessible( clazz, original.getName() ) );
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
