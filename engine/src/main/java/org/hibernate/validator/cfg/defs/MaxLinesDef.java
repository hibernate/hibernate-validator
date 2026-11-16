
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.MaxLines;

/**
 * Constraint definition for {@link MaxLines}.
 *
 * @author Koen Aers
 * @since 9.2
 */
@Incubating
public class MaxLinesDef extends ConstraintDef<MaxLinesDef, MaxLines> {

	public MaxLinesDef() {
		super( MaxLines.class );
	}

	public MaxLinesDef value(int value) {
		addParameter( "value", value );
		return this;
	}
}
