/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import java.text.Normalizer;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Normalized;

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
