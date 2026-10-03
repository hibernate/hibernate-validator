/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.URI;

/**
 * Constraint definition for {@link URI}.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Incubating
public class URIDef extends ConstraintDef<URIDef, URI> {

	public URIDef() {
		super( URI.class );
	}

	public URIDef type(URI.Type type) {
		addParameter( "type", type );
		return this;
	}

	public URIDef schemes(String... schemes) {
		addParameter( "schemes", schemes );
		return this;
	}

	public URIDef authority(URI.AuthorityRequirement authority) {
		addParameter( "authority", authority );
		return this;
	}

	public URIDef allowOpaque(boolean allowOpaque) {
		addParameter( "allowOpaque", allowOpaque );
		return this;
	}

	public URIDef regexp(String regexp) {
		addParameter( "regexp", regexp );
		return this;
	}

	public URIDef flags(Pattern.Flag... flags) {
		addParameter( "flags", flags );
		return this;
	}
}
