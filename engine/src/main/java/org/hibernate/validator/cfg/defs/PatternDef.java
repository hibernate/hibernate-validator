/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * An {@link Pattern} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class PatternDef extends ConstraintDef<PatternDef, Pattern> {

	public PatternDef() {
		super( Pattern.class );
	}

	public PatternDef flags(Pattern.Flag[] flags) {
		addParameter( "flags", flags );
		return this;
	}

	public PatternDef regexp(String regexp) {
		addParameter( "regexp", regexp );
		return this;
	}
}
