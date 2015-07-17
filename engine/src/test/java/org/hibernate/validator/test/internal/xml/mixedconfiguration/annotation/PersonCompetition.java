/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation;

public class PersonCompetition extends Competition {

	public PersonCompetition() {
		super();
	}

	public PersonCompetition(String name) {
		super( name );
	}
}
