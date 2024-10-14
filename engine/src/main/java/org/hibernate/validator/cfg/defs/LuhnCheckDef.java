/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.LuhnCheck;

/**
 * A {@link LuhnCheck} constraint definition.
 *
 * @author Marko Bekta
 */
public class LuhnCheckDef extends ConstraintDef<LuhnCheckDef, LuhnCheck> {

	public LuhnCheckDef() {
		super( LuhnCheck.class );
	}

	public LuhnCheckDef startIndex(int index) {
		addParameter( "startIndex", index );
		return this;
	}

	public LuhnCheckDef endIndex(int index) {
		addParameter( "endIndex", index );
		return this;
	}

	public LuhnCheckDef checkDigitIndex(int index) {
		addParameter( "checkDigitIndex", index );
		return this;
	}

	public LuhnCheckDef ignoreNonDigitCharacters(boolean ignore) {
		addParameter( "ignoreNonDigitCharacters", ignore );
		return this;
	}

}
