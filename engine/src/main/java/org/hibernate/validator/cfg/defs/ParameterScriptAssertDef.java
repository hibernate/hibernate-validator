/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.ParameterScriptAssert;

/**
 * @author Marko Bekta
 */
public class ParameterScriptAssertDef extends ConstraintDef<ParameterScriptAssertDef, ParameterScriptAssert> {

	public ParameterScriptAssertDef() {
		super( ParameterScriptAssert.class );
	}

	public ParameterScriptAssertDef lang(String lang) {
		addParameter( "lang", lang );
		return this;
	}

	public ParameterScriptAssertDef script(String script) {
		addParameter( "script", script );
		return this;
	}

}
