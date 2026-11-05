package org.hibernate.validator.cfg.defs.br;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.br.CPF;

/**
 * A {@link CPF} constraint definition.
 * @author Marko Bekta
 */
public class CPFDef extends ConstraintDef<CPFDef, CPF> {

	public CPFDef() {
		super( CPF.class );
	}

}
