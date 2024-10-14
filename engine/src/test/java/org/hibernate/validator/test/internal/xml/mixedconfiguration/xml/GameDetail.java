/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml.mixedconfiguration.xml;

public class GameDetail {

	private Competition competition;

	public GameDetail() {
		super();
	}

	public GameDetail(Competition competition) {
		setCompetition( competition );
	}

	public Competition getCompetition() {
		return competition;
	}

	public final void setCompetition(Competition competition) {
		this.competition = competition;
	}
}
