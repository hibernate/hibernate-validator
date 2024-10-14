/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.ScriptAssert;

/**
 * A {@link ScriptAssert} constraint definition.
 * @author Hardy Ferentschik
 */
public class ScriptAssertDef extends ConstraintDef<ScriptAssertDef, ScriptAssert> {

	public ScriptAssertDef() {
		super( ScriptAssert.class );
	}

	public ScriptAssertDef lang(String lang) {
		addParameter( "lang", lang );
		return this;
	}

	public ScriptAssertDef script(String script) {
		addParameter( "script", script );
		return this;
	}

	public ScriptAssertDef alias(String alias) {
		addParameter( "alias", alias );
		return this;
	}

	public ScriptAssertDef reportOn(String reportOn) {
		addParameter( "reportOn", reportOn );
		return this;
	}
}
