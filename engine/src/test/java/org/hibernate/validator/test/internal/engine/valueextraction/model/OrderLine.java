/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valueextraction.model;

import jakarta.validation.constraints.Min;

/**
 * @author Gunnar Morling
 */
public class OrderLine {

	private Property<Long> id = new Property<Long>( 0L );

	public OrderLine() {
	}

	public OrderLine(long id) {
		this.id = new Property<Long>( id );
	}

	@Min(1)
	public Property<Long> getId() {
		return id;
	}

	public void setId(@Min(1) Property<Long> id) {
		this.id = id;
	}
}
