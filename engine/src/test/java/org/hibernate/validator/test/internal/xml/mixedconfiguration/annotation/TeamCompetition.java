/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation;

public class TeamCompetition extends Competition {
	public TeamCompetition() {
		super();
	}

	public TeamCompetition(String name) {
		super( name );
	}
}
