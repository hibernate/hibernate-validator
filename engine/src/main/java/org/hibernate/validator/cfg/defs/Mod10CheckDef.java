/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Mod10Check;

/**
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
