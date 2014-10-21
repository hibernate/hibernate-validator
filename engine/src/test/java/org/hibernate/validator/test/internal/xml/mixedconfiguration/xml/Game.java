/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml.mixedconfiguration.xml;

public abstract class Game {

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
