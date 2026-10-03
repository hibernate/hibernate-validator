/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Hexadecimal;
import org.hibernate.validator.constraints.Hexadecimal.HexStrictness;
import org.hibernate.validator.constraints.Hexadecimal.LetterCase;

/**
 * Constraint definition for {@link Hexadecimal}.
 *
 * @since 9.2
 */
@Incubating
public class HexadecimalDef extends ConstraintDef<HexadecimalDef, Hexadecimal> {

	public HexadecimalDef() {
		super( Hexadecimal.class );
	}

	public HexadecimalDef letterCase(LetterCase letterCase) {
		addParameter( "letterCase", letterCase );
		return this;
	}

	public HexadecimalDef strictness(HexStrictness strictness) {
		addParameter( "strictness", strictness );
		return this;
	}

	public HexadecimalDef allowEmpty(boolean allowEmpty) {
		addParameter( "allowEmpty", allowEmpty );
		return this;
	}

	public HexadecimalDef prefix(String prefix) {
		addParameter( "prefix", prefix );
		return this;
	}
}
