
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.UpperCase;

/**
 * Constraint definition for {@link UpperCase}.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Incubating
public class UpperCaseDef extends ConstraintDef<UpperCaseDef, UpperCase> {

	public UpperCaseDef() {
		super( UpperCase.class );
	}
}
