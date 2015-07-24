/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;

import javax.validation.constraints.Pattern;


/**
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
