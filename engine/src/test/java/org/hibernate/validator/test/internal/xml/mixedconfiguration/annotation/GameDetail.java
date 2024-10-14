/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class GameDetail {

	@NotNull
	@Valid
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
