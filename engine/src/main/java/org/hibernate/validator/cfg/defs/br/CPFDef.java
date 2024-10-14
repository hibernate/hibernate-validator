/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
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
