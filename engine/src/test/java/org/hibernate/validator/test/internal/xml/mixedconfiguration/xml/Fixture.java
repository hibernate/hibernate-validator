/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml.mixedconfiguration.xml;

import org.hibernate.validator.test.internal.xml.mixedconfiguration.IFixture;

public class Fixture extends Game implements IFixture {

	public Fixture() {
		super();
	}

	public Fixture(Competition competition) {
		super( competition );
	}

	@Override
	public void setCompetition(Competition competition) {
		super.setCompetition( competition );
	}
}
