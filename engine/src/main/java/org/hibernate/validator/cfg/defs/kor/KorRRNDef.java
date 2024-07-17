/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs.kor;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.kor.KorRRN;

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
