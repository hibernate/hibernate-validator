/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.kor;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.kor.KorRRN;

/**
 * A {@link KorRRN} constraint definition.
 */
@Incubating
public class KorRRNDef extends ConstraintDef<KorRRNDef, KorRRN> {

	public KorRRNDef() {
		super( KorRRN.class );
	}

	public KorRRNDef validateCheckDigit(KorRRN.ValidateCheckDigit validateCheckDigit) {
		addParameter( "validateCheckDigit", validateCheckDigit );
		return this;
	}
}
