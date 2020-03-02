/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
