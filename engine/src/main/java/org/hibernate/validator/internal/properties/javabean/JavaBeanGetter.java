/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.HibernateValidatorPermission;
import org.hibernate.validator.internal.properties.Property;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;

/**
 * @author Marko Bekhta
 */
public class JavaBeanGetter extends JavaBeanExecutable implements Property {

	private static final Class<?>[] PARAMETER_TYPES = new Class[0];

	private final String name;

	/**
	 * The class of the method for which the constraint was defined.
	 * <p>
	 * It is usually the same as the declaring class of the method itself, except in the XML case when a user could
	 * declare a constraint for a specific subclass.
	 */
	private final Class<?> declaringClass;

	public JavaBeanGetter(Method method) {
		super( getAccessible( method ) );
		this.name = ReflectionHelper.getPropertyName( method );
		this.declaringClass = method.getDeclaringClass();
	}

	public JavaBeanGetter(Class<?> declaringClass, Method method) {
		super( getAccessible( method ) );
		this.name = ReflectionHelper.getPropertyName( method );
		this.declaringClass = declaringClass;
	}

	@Override
	public Object getValueFrom(Object bean) {
		return ReflectionHelper.getValue( (Method) executable, bean );
	}

	@Override
	public String getPropertyName() {
		return name;
	}

	@Override
	public boolean hasReturnValue() {
		// getters should always have a return value
		return true;
	}

	@Override
	public boolean hasParameters() {
		// getters should never have parameters
		return false;
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return PARAMETER_TYPES;
	}

	@Override
	public Type[] getGenericParameterTypes() {
		return PARAMETER_TYPES;
	}

	@Override
	public String getParameterName(ExecutableParameterNameProvider parameterNameProvider, int parameterIndex) {
		throw new IllegalStateException( "Getters cannot have parameters" );
	}

	@Override
	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || this.getClass() != o.getClass() ) {
			return false;
		}
		if ( !super.equals( o ) ) {
			return false;
		}

		JavaBeanGetter that = (JavaBeanGetter) o;

		return this.name.equals( that.name );
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + this.name.hashCode();
		return result;
	}

	/**
	 * Returns an accessible copy of the given method.
	 */
	private static Method getAccessible(Method original) {
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
