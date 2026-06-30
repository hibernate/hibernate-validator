/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.NotCompromised;

public class NotCompromisedDef extends ConstraintDef<NotCompromisedDef, NotCompromised> {

	public NotCompromisedDef() {
		super( NotCompromised.class );
	}
}
