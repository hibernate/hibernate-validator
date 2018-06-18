/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean.accessors;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.HibernateValidatorPermission;
import org.hibernate.validator.internal.properties.PropertyAccessor;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;

/**
 * @author Marko Bekhta
 */
public class ReflectionJavaBeanPropertyAccessorFactory implements JavaBeanPropertyAccessorFactory {

	public static ReflectionJavaBeanPropertyAccessorFactory INSTANCE = new ReflectionJavaBeanPropertyAccessorFactory();

	@Override
	public PropertyAccessor forField(Field field) {
		return new FieldAccessor( field );
	}

	@Override
	public PropertyAccessor forGetter(Method getter) {
		return new GetterAccessor( getter );
	}

	private static class FieldAccessor implements PropertyAccessor {

		private Field accessibleField;

		private FieldAccessor(Field field) {
			this.accessibleField = getAccessible( field );
		}

		@Override
		public Object getValueFrom(Object bean) {
			return ReflectionHelper.getValue( accessibleField, bean );
		}
	}

	private static class GetterAccessor implements PropertyAccessor {

		private Method accessibleGetter;

		private GetterAccessor(Method getter) {
			this.accessibleGetter = getAccessible( getter );
		}

		@Override
		public Object getValueFrom(Object bean) {
			return ReflectionHelper.getValue( accessibleGetter, bean );
		}
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
	 * Returns an accessible copy of the given member.
	 */
	private static Field getAccessible(Field original) {
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
