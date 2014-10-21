/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling.model;

import javax.validation.constraints.Size;

import org.hibernate.validator.valuehandling.UnwrapValidatedValue;

/**
 * @author Gunnar Morling
 */
public class Order {

	@Size(min = 4)
	@UnwrapValidatedValue
	private final Wrapper<Long> id = new Wrapper<Long>( 42L );
}
