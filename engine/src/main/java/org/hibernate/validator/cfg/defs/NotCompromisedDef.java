/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.NotCompromised;

/**
 * A {@link NotCompromised} constraint definition.
 *
 * @since 9.2.0
 */
@Incubating
public class NotCompromisedDef extends ConstraintDef<NotCompromisedDef, NotCompromised> {

	public NotCompromisedDef() {
		super( NotCompromised.class );
	}
}
