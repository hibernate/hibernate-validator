/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.br;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.br.TituloEleitoral;

/**
 * A {@link TituloEleitoral} constraint definition.
 *
 * @author Marko Bekta
 */
public class TituloEleitoralDef extends ConstraintDef<TituloEleitoralDef, TituloEleitoral> {

	public TituloEleitoralDef() {
		super( TituloEleitoral.class );
	}

}
