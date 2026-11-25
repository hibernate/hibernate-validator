package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Base64;

/**
 * Constraint definition for {@link Base64}.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Incubating
public class Base64Def extends ConstraintDef<Base64Def, Base64> {

	public Base64Def() {
		super( Base64.class );
	}

	public Base64Def urlSafe(boolean urlSafe) {
		addParameter( "urlSafe", urlSafe );
		return this;
	}
}
