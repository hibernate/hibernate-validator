/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.annotation;

import java.lang.annotation.Annotation;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.NewProxyInstance;

/**
 * Creates live annotations (actually {@link AnnotationProxy} instances) from {@code AnnotationDescriptor}s.
 *
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 * @author Hardy Ferentschik
 * @see AnnotationProxy
 */
public class AnnotationFactory {

	private AnnotationFactory() {
	}

	public static <T extends Annotation> T create(AnnotationDescriptor<T> descriptor) {
		return run( NewProxyInstance.action(
				run( GetClassLoader.fromClass( descriptor.getType() ) ),
				descriptor.getType(),
				new AnnotationProxy( descriptor )
		) );
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
