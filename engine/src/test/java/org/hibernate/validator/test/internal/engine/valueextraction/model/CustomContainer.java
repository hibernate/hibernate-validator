/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import java.util.Iterator;

/**
 * @author Marko Bekhta
 */
public interface CustomContainer<T> {

	Iterator<T> iterator();
}
