/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.xml.mixedconfiguration.annotation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public abstract class Game {

	@NotNull
	@Valid
	private GameDetail detail;

	private Game(GameDetail detail) {
		this.detail = detail;
	}

	public Game() {
		this( new GameDetail() );
	}

	public Game(Competition competition) {
		this( new GameDetail( competition ) );
	}

	public Competition getCompetition() {
		return detail.getCompetition();
	}

	public void setCompetition(Competition competition) {
		detail.setCompetition( competition );
	}
}
