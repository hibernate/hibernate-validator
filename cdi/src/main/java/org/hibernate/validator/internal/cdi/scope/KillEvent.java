/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cdi.scope;

/**
 * @author Hardy Ferentschik
 */
public class KillEvent {
	private Class beanType;

	public KillEvent(Class beanType) {
		this.beanType = beanType;
	}

	public Class getBeanType() {
		return beanType;
	}

}
