/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.br;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.br.RENAVAM;

/**
 * A {@link RENAVAM} constraint definition.
 *
 * @author Matheus Pereira
 * @since 9.2
 */
@Incubating
public class RENAVAMDef extends ConstraintDef<RENAVAMDef, RENAVAM> {

	public RENAVAMDef() {
		super( RENAVAM.class );
	}
}
