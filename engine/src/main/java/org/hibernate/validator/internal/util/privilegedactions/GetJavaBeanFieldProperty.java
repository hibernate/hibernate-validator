/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.properties.javabean.JavaBeanField;

/**
 * Returns the {@link Field} property from the {@link JavaBeanField} instance.
 *
 * @author Marko Bekhta
 */
public final class GetJavaBeanFieldProperty implements PrivilegedAction<Field> {

	private static final MethodHandle fieldGetter;

	static {
		fieldGetter = run( GetDeclaredFieldValueMethodHandle.action( MethodHandles.lookup(), JavaBeanField.class, "field", true ) );
	}

	private final JavaBeanField field;

	public GetJavaBeanFieldProperty(JavaBeanField field) {
		this.field = field;
	}

	public static GetJavaBeanFieldProperty action(JavaBeanField field) {
		return new GetJavaBeanFieldProperty( field );
	}

	@Override
	public Field run() {
		try {
			return (Field) fieldGetter.invoke( field );
		}
		catch (Throwable throwable) {
			throw new IllegalStateException( "Unable to retrieve value from method handle", throwable );
		}
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
