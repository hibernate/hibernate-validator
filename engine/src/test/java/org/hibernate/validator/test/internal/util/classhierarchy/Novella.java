/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.util.classhierarchy;

import org.hibernate.validator.test.internal.util.ExecutableHelperTest.Literature;

/**
 * This class is used to test {@code ExecutableHelper#overrides(java.lang.reflect.Method, java.lang.reflect.Method)}.
 * The scenario represents a subclass with a method that has the same signature of the superclass but different
 * visibility.
 *
 * @see org.hibernate.validator.internal.util.ExecutableHelper
 * @see org.hibernate.validator.test.internal.util.ExecutableHelperTest
 * @author Davide D'Alto
 */
public class Novella extends Literature {

	/*
	 * This method has the same signature of Literature#getTitle() but it does not override it becuase in the superclass
	 * the method is package private.
	 */
	public String getTitle() {
		return null;
	}
}
