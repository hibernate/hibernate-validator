/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Returns the {@link MethodHandle} getter for declared field value with the specified name,
 * throws an exception if it does not exist or cannot be accessed.
 *
 * @author Marko Bekhta
 */
public final class GetDeclaredFieldValueMethodHandle implements PrivilegedAction<MethodHandle> {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final Lookup lookup;
	private final Class<?> clazz;
	private final String property;
	private final boolean makeAccessible;

	/**
	 * Before using this method on arbitrary classes, you need to check the {@code HibernateValidatorPermission.ACCESS_PRIVATE_MEMBERS}
	 * permission against the security manager, if the calling class exposes the handle to clients.
	 */
	public static GetDeclaredFieldValueMethodHandle action(Lookup lookup, Class<?> clazz, String property, boolean makeAccessible) {
		return new GetDeclaredFieldValueMethodHandle( lookup, clazz, property, makeAccessible );
	}

	private GetDeclaredFieldValueMethodHandle(Lookup lookup, Class<?> clazz, String property, boolean makeAccessible) {
		this.lookup = lookup;
		this.clazz = clazz;
		this.property = property;
		this.makeAccessible = makeAccessible;
	}

	@Override
	public MethodHandle run() {
		try {
			Field field = clazz.getDeclaredField( property );
			if ( makeAccessible ) {
				field.setAccessible( true );
			}
			return lookup.unreflectGetter( field );
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			throw LOG.getUnableToAccessFieldException( lookup, clazz, property, e );
		}
	}
}
