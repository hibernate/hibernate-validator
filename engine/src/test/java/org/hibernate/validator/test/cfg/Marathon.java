/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

