/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.cfg.defs;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Email;

/**
 * @author Hardy Ferentschik
 */
public class EmailDef extends ConstraintDef<EmailDef, Email> {

	public EmailDef() {
		super( Email.class );
	}

	public EmailDef regexp(String regexp) {
		addParameter( "regexp", regexp );
		return this;
	}

	public EmailDef flags(Pattern.Flag... flags) {
		addParameter( "flags", flags );
		return this;
	}
}
