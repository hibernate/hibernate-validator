/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.testmodel.circular;

/**
 * @author Marko Bekhta
 */
public interface CircularProperty {

	void doSomething();

	interface ImprovedCircularProperty extends CircularProperty {

		void maybeDoSomething();
	}
}
