/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.NullOrNotBlank;

/**
 * Constraint definition for {@link NullOrNotBlank}.
 * @author Koen Aers
 * @since 9.1
 */
public class NullOrNotBlankDef extends ConstraintDef<NullOrNotBlankDef, NullOrNotBlank> {

	public NullOrNotBlankDef() {
		super( NullOrNotBlank.class );
	}
}
