/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Mod10Check;

/**
 * A {@link Mod10Check} constraint definition.
 * @author Hardy Ferentschik
 */
public class Mod10CheckDef extends ConstraintDef<Mod10CheckDef, Mod10Check> {

	public Mod10CheckDef() {
		super( Mod10Check.class );
	}

	public Mod10CheckDef multiplier(int multiplier) {
		addParameter( "multiplier", multiplier );
		return this;
	}

	public Mod10CheckDef weight(int weight) {
		addParameter( "weight", weight );
		return this;
	}

	public Mod10CheckDef startIndex(int startIndex) {
		addParameter( "startIndex", startIndex );
		return this;
	}

	public Mod10CheckDef endIndex(int endIndex) {
		addParameter( "endIndex", endIndex );
		return this;
	}

	public Mod10CheckDef checkDigitIndex(int checkDigitIndex) {
		addParameter( "checkDigitIndex", checkDigitIndex );
		return this;
	}

	public Mod10CheckDef ignoreNonDigitCharacters(boolean ignoreNonDigitCharacters) {
		addParameter( "ignoreNonDigitCharacters", ignoreNonDigitCharacters );
		return this;
	}
}
