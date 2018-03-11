/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.lang.reflect.Method;
import java.security.PrivilegedAction;

import org.hibernate.validator.properties.GetterPropertyMatcher;

/**
 * Returns the method with the specified property name or {@code null} if it does not exist. This method will prepend
 * 'is' and 'get' to the property name and capitalize the first letter.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Marko Bekhta
 */
public final class GetMethodFromPropertyName implements PrivilegedAction<Method> {
	private final Class<?> clazz;
	private final GetterPropertyMatcher getterPropertyMatcher;
	private final String property;

	public static GetMethodFromPropertyName action(Class<?> clazz, GetterPropertyMatcher getterPropertyMatcher, String property) {
		return new GetMethodFromPropertyName( clazz, getterPropertyMatcher, property );
	}

	private GetMethodFromPropertyName(Class<?> clazz, GetterPropertyMatcher getterPropertyMatcher, String property) {
		this.clazz = clazz;
		this.getterPropertyMatcher = getterPropertyMatcher;
		this.property = property;
	}

	@Override
	public Method run() {
		for ( String possibleName : getterPropertyMatcher.getPossibleMethodNamesForProperty( property ) ) {
			try {
				return clazz.getMethod( possibleName );
			}
			catch (NoSuchMethodException e) {
				// silently ignore the exception
			}
		}
		return null;
	}
}
