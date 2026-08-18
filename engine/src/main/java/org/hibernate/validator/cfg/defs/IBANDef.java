/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.IBAN;

/**
 * An {@link IBAN} constraint definition.
 *
 * @author Andrea Boriero
 * @since 9.2
 */
public class IBANDef extends ConstraintDef<IBANDef, IBAN> {

	public IBANDef() {
		super( IBAN.class );
	}
}
