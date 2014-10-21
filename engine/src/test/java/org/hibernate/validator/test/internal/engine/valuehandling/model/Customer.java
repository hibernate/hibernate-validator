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
public class Customer {

	@Size(min = 4)
	@UnwrapValidatedValue
	private Property<String> name = new Property<String>( "Bob" );

	@Size(min = 4)
	@UnwrapValidatedValue
	private final StringProperty lastName = new StringProperty( "Foo" );

	@Size(min = 4, groups = CustomValidationGroup.class)
	@UnwrapValidatedValue
	private final StringProperty middleName = new StringProperty( "Foo" );

	@Size(min = 4)
	@UnwrapValidatedValue
	private final UiInput<String> nameInput = new UiInput<String>( "Bob" );

	public void setName(@Size(min = 4) @UnwrapValidatedValue Property<String> name) {
		this.name = name;
	}

	@Size(min = 4)
	@UnwrapValidatedValue
	public Property<String> retrieveName() {
		return name;
	}

	public interface CustomValidationGroup {
	}
}
