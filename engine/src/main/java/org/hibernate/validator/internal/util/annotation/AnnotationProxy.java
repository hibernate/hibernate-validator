/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.annotation;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.validator.internal.util.privilegedactions.GetAnnotationAttributes;

/**
 * A concrete implementation of {@code Annotation} that pretends it is a
 * "real" source code annotation. It's also an {@code InvocationHandler}.
 * <p>
 * When you create an {@code AnnotationProxy}, you must initialize it
 * with an {@code AnnotationDescriptor}.
 * The adapter checks that the provided elements are the same elements defined
 * in the annotation interface. However, it does <i>not</i> check that their
 * values are the right type. If you omit an element, the adapter will use the
 * default value for that element from the annotation interface, if it exists.
 * If no default exists, it will throw an exception.
 * </p>
 *
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 * @author Gunnar Morling
 * @author Guillaume Smet
 * @see java.lang.annotation.Annotation
 */
class AnnotationProxy implements Annotation, InvocationHandler, Serializable {

	private static final long serialVersionUID = 6907601010599429454L;

	private final AnnotationDescriptor<? extends Annotation> descriptor;

	AnnotationProxy(AnnotationDescriptor<? extends Annotation> descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object value = descriptor.getAttribute( method.getName() );
		if ( value != null ) {
			return value;
		}
		return method.invoke( this, args );
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return descriptor.getType();
	}

	/**
	 * Performs an equality check as described in {@link Annotation#equals(Object)}.
	 *
	 * @param obj The object to compare
	 *
	 * @return Whether the given object is equal to this annotation proxy or not
	 *
	 * @see Annotation#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( !descriptor.getType().isInstance( obj ) ) {
			return false;
		}

		Annotation other = descriptor.getType().cast( obj );

		Map<String, Object> otherAttributes = getAnnotationAttributes( other );

		if ( descriptor.getAttributes().size() != otherAttributes.size() ) {
			return false;
		}

		// compare annotation member values
		for ( Entry<String, Object> member : descriptor.getAttributes().entrySet() ) {
			Object value = member.getValue();
			Object otherValue = otherAttributes.get( member.getKey() );

			if ( !areEqual( value, otherValue ) ) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Calculates the hash code of this annotation proxy as described in
	 * {@link Annotation#hashCode()}.
	 *
	 * @return The hash code of this proxy.
	 *
	 * @see Annotation#hashCode()
	 */
	@Override
	public int hashCode() {
		return descriptor.hashCode();
	}

	@Override
	public String toString() {
		return descriptor.toString();
	}

	private boolean areEqual(Object o1, Object o2) {
		return
				!o1.getClass().isArray() ? o1.equals( o2 )
						: o1.getClass() == boolean[].class ? Arrays.equals( (boolean[]) o1, (boolean[]) o2 )
						: o1.getClass() == byte[].class ? Arrays.equals( (byte[]) o1, (byte[]) o2 )
						: o1.getClass() == char[].class ? Arrays.equals( (char[]) o1, (char[]) o2 )
						: o1.getClass() == double[].class ? Arrays.equals( (double[]) o1, (double[]) o2 )
						: o1.getClass() == float[].class ? Arrays.equals( (float[]) o1, (float[]) o2 )
						: o1.getClass() == int[].class ? Arrays.equals( (int[]) o1, (int[]) o2 )
						: o1.getClass() == long[].class ? Arrays.equals( (long[]) o1, (long[]) o2 )
						: o1.getClass() == short[].class ? Arrays.equals( (short[]) o1, (short[]) o2 )
						: Arrays.equals( (Object[]) o1, (Object[]) o2 );
	}

	private Map<String, Object> getAnnotationAttributes(Annotation annotation) {
		// We only enable this optimization if the security manager is not enabled. Otherwise,
		// we would have to add every package containing constraints to the security policy.
		if ( Proxy.isProxyClass( annotation.getClass() ) && System.getSecurityManager() == null ) {
			InvocationHandler invocationHandler = Proxy.getInvocationHandler( annotation );
			if ( invocationHandler instanceof AnnotationProxy ) {
				return ( (AnnotationProxy) invocationHandler ).descriptor.getAttributes();
			}
		}

		return run( GetAnnotationAttributes.action( annotation ) );
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
