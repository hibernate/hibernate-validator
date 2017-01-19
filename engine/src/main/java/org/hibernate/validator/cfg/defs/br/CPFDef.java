/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg.defs.br;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.br.CPF;

/**
 * @author Marko Bekta
 */
public class CPFDef extends ConstraintDef<CPFDef, CPF> {

	public CPFDef() {
		super( CPF.class );
	}

}
