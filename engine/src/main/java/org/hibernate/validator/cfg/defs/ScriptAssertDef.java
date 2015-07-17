/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.ScriptAssert;

/**
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
}
