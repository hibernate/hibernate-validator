/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.URL;

/**
 * An {@link URL} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class URLDef extends ConstraintDef<URLDef, URL> {

	public URLDef() {
		super( URL.class );
	}

	public URLDef protocol(String protocol) {
		addParameter( "protocol", protocol );
		return this;
	}

	public URLDef host(String host) {
		addParameter( "host", host );
		return this;
	}

	public URLDef port(int port) {
		addParameter( "port", port );
		return this;
	}

	public URLDef regexp(String regexp) {
		addParameter( "regexp", regexp );
		return this;
	}

	public URLDef flags(Pattern.Flag... flags) {
		addParameter( "flags", flags );
		return this;
	}
}
