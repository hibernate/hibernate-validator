/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.cascaded;

import jakarta.validation.Valid;

/**
 * @author Gunnar Morling
 *
 */
class Cinema {

	String name;

	Reference<@Valid Visitor> visitor;

	Cinema() {
	}

	Cinema(String name, Reference<Visitor> visitor) {
		this.name = name;
		this.visitor = visitor;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append( getClass().getSimpleName() )
				.append( "<" ).append( name ).append( ">" );
		return sb.toString();
	}
}
