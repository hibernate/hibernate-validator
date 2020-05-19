/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Normalized;

import java.text.Normalizer;


/**
 * Constraint definition for {@link Normalized}.
 * @author Craig Andrews
 * @since 6.1.6
 */
public class NormalizedDef extends ConstraintDef<NormalizedDef, Normalized> {

	public NormalizedDef() {
		super( Normalized.class );
	}

	public NormalizedDef normalizationStrategy(Normalizer.Form form) {
		addParameter( "normalizationForm", form );
		return this;
	}
}
