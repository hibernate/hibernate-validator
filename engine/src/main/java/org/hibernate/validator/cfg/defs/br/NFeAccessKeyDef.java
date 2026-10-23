/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.br;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.br.NFeAccessKey;

/**
 * A {@link NFeAccessKey} constraint definition.
 *
 * @author Matheus Pereira
 * @since 9.2
 */
@Incubating
public class NFeAccessKeyDef extends ConstraintDef<NFeAccessKeyDef, NFeAccessKey> {

	public NFeAccessKeyDef() {
		super( NFeAccessKey.class );
	}
}
