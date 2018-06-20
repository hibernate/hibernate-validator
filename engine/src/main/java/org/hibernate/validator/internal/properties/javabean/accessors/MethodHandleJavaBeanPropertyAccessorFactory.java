/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties.javabean.accessors;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.HibernateValidatorPermission;
import org.hibernate.validator.internal.properties.PropertyAccessor;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;

/**
 * @author Marko Bekhta
 */
public class MethodHandleJavaBeanPropertyAccessorFactory implements JavaBeanPropertyAccessorFactory {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final MethodHandles.Lookup lookup;

	public MethodHandleJavaBeanPropertyAccessorFactory(MethodHandles.Lookup lookup) {
		this.lookup = lookup;
	}


	@Override
	public PropertyAccessor forField(Field field) {
		return new FieldAccessor( lookup, field );
	}

	@Override
	public PropertyAccessor forGetter(Method getter) {
		return new GetterAccessor( lookup, getter );
	}

	private abstract static class AbstractAccessor implements PropertyAccessor {

		protected final MethodHandle accessibleFieldHandle;

		private AbstractAccessor(MethodHandle accessibleFieldHandle) {
			this.accessibleFieldHandle = accessibleFieldHandle;
		}

		@Override
		public Object getValueFrom(Object bean) {
			return ReflectionHelper.getValue( accessibleFieldHandle, bean );
		}
	}

	private static class FieldAccessor extends AbstractAccessor {

		private FieldAccessor(MethodHandles.Lookup lookup, Field field) {
			super( unreflectField( lookup, field ) );
		}

		private static MethodHandle unreflectField(MethodHandles.Lookup lookup, Field field) {
			try {
				return lookup.unreflectGetter( getAccessible( field ) );
			}
			catch (IllegalAccessException e) {
				throw LOG.getCannotCustructMethodHandleForMember( field );
			}
		}
	}

	private static class GetterAccessor extends AbstractAccessor {

		private GetterAccessor(MethodHandles.Lookup lookup, Method method) {
			super( unreflectMethod( lookup, method ) );
		}

		private static MethodHandle unreflectMethod(MethodHandles.Lookup lookup, Method method) {
			try {
				return lookup.unreflect( getAccessible( method ) );
			}
			catch (IllegalAccessException e) {
				throw LOG.getCannotCustructMethodHandleForMember( method );
			}
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
