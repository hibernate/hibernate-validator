package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.NormalizedWhitespace;

/**
 * A {@link NormalizedWhitespace} constraint definition.
 *
 * @author Koen Aers
 * @since 9.2
 */
@Incubating
public class NormalizedWhitespaceDef extends ConstraintDef<NormalizedWhitespaceDef, NormalizedWhitespace> {

	public NormalizedWhitespaceDef() {
		super( NormalizedWhitespace.class );
	}

	public NormalizedWhitespaceDef value(NormalizedWhitespace.Rule... rules) {
		addParameter( "value", rules );
		return this;
	}
}
