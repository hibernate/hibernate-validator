/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.ParameterScriptAssert;

/**
 * A {@link ParameterScriptAssert} constraint definition.
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
