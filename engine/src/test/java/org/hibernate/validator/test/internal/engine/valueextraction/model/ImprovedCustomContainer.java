/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

/**
 * @author Marko Bekhta
 */
public interface ImprovedCustomContainer<T,E> extends CustomContainer<T> {
	int size();

	E getE();
}
