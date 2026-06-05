/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.Port;

/**
 * Constraint definition for {@link Port}.
 * @author Koen Aers
 * @since 9.2
 */
public class PortDef extends ConstraintDef<PortDef, Port> {

	public PortDef() {
		super( Port.class );
	}
}
