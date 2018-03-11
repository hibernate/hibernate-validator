/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.properties;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Marko Bekhta
 */
public interface GetterPropertyMatcher {

	boolean isProperty(Executable executable);

	String getPropertyName(Method method);

	Set<String> getPossibleMethodNamesForProperty(String propertyName);
}
