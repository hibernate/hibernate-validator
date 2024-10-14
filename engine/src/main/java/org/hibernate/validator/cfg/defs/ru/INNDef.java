/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.cfg.defs.ru;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.constraints.ru.INN;

/**
 * An {@link INN} constraint definition.
 *
 * @author Artem Boiarshinov
 */
public class INNDef extends ConstraintDef<INNDef, INN> {

	public INNDef() {
		super( INN.class );
	}

	public INNDef type(INN.Type type) {
		addParameter( "type", type );
		return this;
	}
}
