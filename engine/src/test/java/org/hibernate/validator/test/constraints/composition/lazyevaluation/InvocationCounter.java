/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.constraints.composition.lazyevaluation;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Hardy Ferentschik
 */
public class InvocationCounter {
	public static final Map<Object, Integer> countsPerInstance = new HashMap<Object, Integer>();

	public static int getInvocationCount(Object o) {
		return countsPerInstance.get( o );
	}

	public synchronized void incrementCount(Object o) {
		Integer count = countsPerInstance.get( o );
		if ( count == null ) {
			countsPerInstance.put( o, 1 );
		}
		else {
			countsPerInstance.put( o, count + 1 );
		}
	}
}
