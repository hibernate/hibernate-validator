package org.hibernate.validator.referenceguide.chapter12.constraintapi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

