/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.br;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.br.CNPJ;

/**
 * A {@link CNPJ} constraint definition.
 * @author Marko Bekta
 */
public class CNPJDef extends ConstraintDef<CNPJDef, CNPJ> {

	public CNPJDef() {
		super( CNPJ.class );
	}

}
