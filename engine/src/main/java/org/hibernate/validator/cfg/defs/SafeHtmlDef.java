/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.SafeHtml;

/**
 * @author Marko Bekta
 */
public class SafeHtmlDef extends ConstraintDef<SafeHtmlDef, SafeHtml> {

	public SafeHtmlDef() {
		super( SafeHtml.class );
	}

	public SafeHtmlDef whitelistType(SafeHtml.WhiteListType whitelistType) {
		addParameter( "whitelistType", whitelistType );
		return this;
	}

	public SafeHtmlDef additionalTags(String... additionalTags) {
		addParameter( "additionalTags", additionalTags );
		return this;
	}

	public SafeHtmlDef additionalTagsWithAttributes(SafeHtml.Tag... additionalTagsWithAttributes) {
		addParameter( "additionalTagsWithAttributes", additionalTagsWithAttributes );
		return this;
	}

}

