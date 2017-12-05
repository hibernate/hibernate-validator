/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.location;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.HibernateValidatorPermission;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.StringHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;

/**
 * Getter method constraint location.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class GetterConstraintLocation implements ConstraintLocation {

	/**
	 * The method the constraint was defined on.
	 */
	private final Method method;

	private final Method accessibleMethod;

	/**
	 * The property name associated with the method.
	 */
	private final String propertyName;

	/**
	 * The type to be used for validator resolution for constraints at this location.
	 */
	private final Type typeForValidatorResolution;


	GetterConstraintLocation(Method method) {
		this.method = method;
		this.accessibleMethod = getAccessible( method );
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
		return ReflectionHelper.getValue( accessibleMethod, parent );
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

		GetterConstraintLocation that = (GetterConstraintLocation) o;

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
		int result = method != null ? method.hashCode() : 0;
		result = 31 * result + typeForValidatorResolution.hashCode();
		return result;
	}

	/**
	 * Returns an accessible version of the given method. Will be the given method itself in case it is accessible,
	 * otherwise a copy which is set accessible.
	 */
	private static Method getAccessible(Method original) {
		if ( ( (AccessibleObject) original ).isAccessible() ) {
			return original;
		}

		SecurityManager sm = System.getSecurityManager();
		if ( sm != null ) {
			sm.checkPermission( HibernateValidatorPermission.ACCESS_PRIVATE_MEMBERS );
		}

		Class<?> clazz = original.getDeclaringClass();
		Method accessibleMethod = run( GetDeclaredMethod.andMakeAccessible( clazz, original.getName() ) );

		return accessibleMethod;
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
