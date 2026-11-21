
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.SingleLine;

/**
 * Constraint definition for {@link SingleLine}.
 *
 * @author Koen Aers
 * @since 9.2
 */
@Incubating
public class SingleLineDef extends ConstraintDef<SingleLineDef, SingleLine> {

	public SingleLineDef() {
		super( SingleLine.class );
	}
}
