/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Marko Bekhta
 */
public class Foo {

	@NotNull
	@NotBlank
	@Size(min = 5)
	private String string;

	public Foo(String string) {
		this.string = string;
	}

	@AssertTrue
	public boolean isTrue() {
		return false;
	}
}
