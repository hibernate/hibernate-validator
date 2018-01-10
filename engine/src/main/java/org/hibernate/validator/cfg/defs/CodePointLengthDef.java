/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.CodePointLength;


/**
 * Constraint definition for {@link CodePointLength}.
 * @author Kazuki Shimizu
 * @since 6.0.3
 */
public class CodePointLengthDef extends ConstraintDef<CodePointLengthDef, CodePointLength> {

	public CodePointLengthDef() {
		super( CodePointLength.class );
	}

	public CodePointLengthDef min(int min) {
		addParameter( "min", min );
		return this;
	}

	public CodePointLengthDef max(int max) {
		addParameter( "max", max );
		return this;
	}

	public CodePointLengthDef normalizationStrategy(CodePointLength.NormalizationStrategy strategy) {
		addParameter( "normalizationStrategy", strategy );
		return this;
	}
}
