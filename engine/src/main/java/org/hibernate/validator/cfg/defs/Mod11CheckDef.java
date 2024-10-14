/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Mod11Check;

/**
 * A {@link Mod11Check} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class Mod11CheckDef extends ConstraintDef<Mod11CheckDef, Mod11Check> {

	public Mod11CheckDef() {
		super( Mod11Check.class );
	}

	public Mod11CheckDef threshold(int threshold) {
		addParameter( "threshold", threshold );
		return this;
	}

	public Mod11CheckDef startIndex(int startIndex) {
		addParameter( "startIndex", startIndex );
		return this;
	}

	public Mod11CheckDef endIndex(int endIndex) {
		addParameter( "endIndex", endIndex );
		return this;
	}

	public Mod11CheckDef checkDigitIndex(int checkDigitIndex) {
		addParameter( "checkDigitIndex", checkDigitIndex );
		return this;
	}

	public Mod11CheckDef ignoreNonDigitCharacters(boolean ignoreNonDigitCharacters) {
		addParameter( "ignoreNonDigitCharacters", ignoreNonDigitCharacters );
		return this;
	}

	public Mod11CheckDef treatCheck10As(char treatCheck10As) {
		addParameter( "treatCheck10As", treatCheck10As );
		return this;
	}

	public Mod11CheckDef treatCheck11As(char treatCheck11As) {
		addParameter( "treatCheck11As", treatCheck11As );
		return this;
	}

	public Mod11CheckDef processingDirection(Mod11Check.ProcessingDirection processingDirection) {
		addParameter( "processingDirection", processingDirection );
		return this;
	}
}
