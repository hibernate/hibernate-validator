/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.URL;

/**
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
