/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * An {@link Email} constraint definition.
 *
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
