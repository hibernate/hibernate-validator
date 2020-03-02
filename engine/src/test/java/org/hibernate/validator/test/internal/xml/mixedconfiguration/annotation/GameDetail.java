/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
