/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.SetAccessibility;

/**
 * Property constraint location (field or getter).
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class PropertyConstraintLocation implements ConstraintLocation {

	/**
	 * The member the constraint was defined on.
	 */
	private final Member member;

	private final Member accessibleMember;

	/**
	 * The property name associated with the member.
	 */
	private final String propertyName;

	/**
	 * The type to be used for validator resolution for constraints at this location.
	 */
	private final Type typeForValidatorResolution;


	PropertyConstraintLocation(Member member) {
		this.member = member;
		this.accessibleMember = getAccessible( member );
		this.propertyName = ReflectionHelper.getPropertyName( member );
		this.typeForValidatorResolution = ReflectionHelper.boxedType( ReflectionHelper.typeOf( member ) );
	}

	@Override
	public Class<?> getDeclaringClass() {
		return member.getDeclaringClass();
	}

	@Override
	public Member getMember() {
		return member;
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
	// TODO Probably this should be done with a security check to prevent direct usage by external clients
	public Object getValue(Object parent) {
		if ( accessibleMember instanceof Method ) {
			return ReflectionHelper.getValue( (Method) accessibleMember, parent );
		}
		else if ( accessibleMember instanceof Field ) {
			return ReflectionHelper.getValue( (Field) accessibleMember, parent );
		}
		else {
			throw new IllegalArgumentException( "Unexpected member type: " + accessibleMember );
		}
	}

	@Override
	public String toString() {
		return "PropertyConstraintLocation [member=" + StringHelper.toShortString( member ) + ", typeForValidatorResolution="
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

		PropertyConstraintLocation that = (PropertyConstraintLocation) o;

		if ( member != null ? !member.equals( that.member ) : that.member != null ) {
			return false;
		}
		if ( !typeForValidatorResolution.equals( that.typeForValidatorResolution ) ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = member != null ? member.hashCode() : 0;
		result = 31 * result + typeForValidatorResolution.hashCode();
		return result;
	}

	/**
	 * Returns an accessible version of the given member. Will be the given member itself in case it is accessible,
	 * otherwise a copy which is set accessible.
	 */
	private static Member getAccessible(Member original) {
		if ( ( (AccessibleObject) original ).isAccessible() ) {
			return original;
		}

		Class<?> clazz = original.getDeclaringClass();
		Member accessibleMember;

		if ( original instanceof Field ) {
			accessibleMember = run( GetDeclaredField.action( clazz, original.getName() ) );
		}
		else {
			accessibleMember = run( GetDeclaredMethod.action( clazz, original.getName() ) );
		}

		run( SetAccessibility.action( accessibleMember ) );

		return accessibleMember;
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
