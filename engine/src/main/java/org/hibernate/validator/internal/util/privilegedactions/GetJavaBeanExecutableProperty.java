/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Executable;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.properties.javabean.JavaBeanExecutable;

/**
 * Returns the {@link Executable} property from the {@link JavaBeanExecutable} instance.
 *
 * @author Marko Bekhta
 */
public final class GetJavaBeanExecutableProperty implements PrivilegedAction<Executable> {

	private static final MethodHandle fieldGetter;

	static {
		fieldGetter = run( GetDeclaredFieldValueMethodHandle.action( MethodHandles.lookup(), JavaBeanExecutable.class, "executable", true ) );
	}

	private final JavaBeanExecutable executable;

	public GetJavaBeanExecutableProperty(JavaBeanExecutable executable) {
		this.executable = executable;
	}

	public static GetJavaBeanExecutableProperty action(JavaBeanExecutable field) {
		return new GetJavaBeanExecutableProperty( field );
	}

	@Override
	public Executable run() {
		try {
			return (Executable) fieldGetter.invoke( executable );
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
