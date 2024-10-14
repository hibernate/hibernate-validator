/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
