/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs;

import jakarta.validation.constraints.NotBlank;

import org.hibernate.validator.cfg.ConstraintDef;

/**
 * A {@link NotBlank} constraint definition.
 *
 * @author Hardy Ferentschik
 */
public class NotBlankDef extends ConstraintDef<NotBlankDef, NotBlank> {

	public NotBlankDef() {
		super( NotBlank.class );
	}
}
