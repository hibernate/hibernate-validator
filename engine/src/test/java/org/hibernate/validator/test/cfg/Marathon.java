/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cfg;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Hardy Ferentschik
 */
public class Marathon implements Tournament {

	private String name;

	/**
	 * Intentionally without a getter/setter to test adding a constraint programmatically using field access type
	 */
	@SuppressWarnings("unused")
	private long numberOfHelpers;

	private Date tournamentDate;

	private final List<Runner> runners;

	public Marathon() {
		runners = new ArrayList<Runner>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Date getTournamentDate() {
		return tournamentDate;
	}

	public void setTournamentDate(Date tournamentDate) {
		this.tournamentDate = tournamentDate;
	}

	public List<Runner> getRunners() {
		return runners;
	}

	public boolean addRunner(Runner runner) {
		return runners.add( runner );
	}
}

