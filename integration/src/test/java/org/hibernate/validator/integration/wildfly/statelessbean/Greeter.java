/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.statelessbean;

import javax.inject.Inject;

@javax.faces.bean.ManagedBean(name = "greeter", eager = true)
public class Greeter {

	@Inject
	private StatelessBeanInterface bean;

	public String getMessage() {
		return bean.lookup( null );
	}
}
