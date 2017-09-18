/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.property;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Default property accessor which follows java bean naming convention.
 */
public class JavaBeanPropertySelector implements PropertyAccessorSelector {

	private static final Log log = LoggerFactory.make();

	@Override
	public String getPropertyName(Method method) {
		return ReflectionHelper.getPropertyName( method );
	}

	@Override
	public boolean isGetterMethod(Method method) {
		return ReflectionHelper.isGetterMethod( method );
	}

	@Override
	public Method findMethod(Class<?> clazz, String property) {
		Contracts.assertNotNull( clazz, MESSAGES.classCannotBeNull() );

		if ( property == null || property.isEmpty() ) {
			throw log.getPropertyNameCannotBeNullOrEmptyException();

		}

		final String methodName = property.substring( 0, 1 ).toUpperCase() + property.substring( 1 );
		for ( String prefix : ReflectionHelper.PROPERTY_ACCESSOR_PREFIXES ) {
			Method method = run( GetMethod.action( clazz, prefix + methodName ) );
			if ( method != null ) {
				return method;
			}
		}

		return null;
	}

	@Override
	public boolean supports(Method method) {
		return ReflectionHelper.isGetterMethod( method );
	}

	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}

}
