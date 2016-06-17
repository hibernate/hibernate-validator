/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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


