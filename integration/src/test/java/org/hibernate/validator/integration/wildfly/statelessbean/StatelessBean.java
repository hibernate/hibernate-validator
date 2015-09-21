/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.integration.wildfly.statelessbean;

import javax.ejb.Stateless;

@Stateless
public class StatelessBean implements StatelessBeanInterface {

	public static final String NULL_MESSAGE = "ERROR （；¬＿¬)";

	@Override
	public String lookup(String text) {
		if ( text == null ) {
			return NULL_MESSAGE;
		}
		else {
			return "Found me!";
		}
	}
}
