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

		void objectValue(Object object);

		void listValue(int i, Object object);

		void mapValue(Object object, Object key);

		void mapKey(Object key);

		void iterableValue(Object object);
	}
}
