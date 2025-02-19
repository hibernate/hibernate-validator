/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg.defs;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.UUID;

/**
 * Constraint definition for {@link UUID}.
 *
 * @author Daniel Heid
 * @since 8.0.0
 */
public class UUIDDef extends ConstraintDef<UUIDDef, UUID> {

	public UUIDDef() {
		super( UUID.class );
	}

	public UUIDDef allowEmpty(boolean allowEmpty) {
		addParameter( "allowEmpty", allowEmpty );
		return this;
	}

	public UUIDDef allowNil(boolean allowNil) {
		addParameter( "allowNil", allowNil );
		return this;
	}

	public UUIDDef version(int[] version) {
		addParameter( "version", version );
		return this;
	}

	public UUIDDef variant(int[] variant) {
		addParameter( "variant", variant );
		return this;
	}

	public UUIDDef letterCase(UUID.LetterCase letterCase) {
		addParameter( "letterCase", letterCase );
		return this;
	}
}
