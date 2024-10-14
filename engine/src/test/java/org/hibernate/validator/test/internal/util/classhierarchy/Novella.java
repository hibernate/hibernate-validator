/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
