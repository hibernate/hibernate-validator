/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.cascading;

public interface ValueExtractor<T> {

	void extractValues(T originalValue, ValueReceiver receiver);

	interface ValueReceiver {

		void value(Object object, String nodeName);

		void iterableValue(Object object, String nodeName);

		void indexedValue(Object object, String nodeName, int i);

		void keyedValue(Object object, String nodeName, Object key);
	}
}
