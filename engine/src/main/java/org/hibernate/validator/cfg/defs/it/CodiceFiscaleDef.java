package org.hibernate.validator.cfg.defs.it;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.it.CodiceFiscale;

/**
 * A {@link CodiceFiscale} constraint definition.
 *
 * @since 9.2
 */
@Incubating
public class CodiceFiscaleDef extends ConstraintDef<CodiceFiscaleDef, CodiceFiscale> {

	public CodiceFiscaleDef() {
		super( CodiceFiscale.class );
	}
}
