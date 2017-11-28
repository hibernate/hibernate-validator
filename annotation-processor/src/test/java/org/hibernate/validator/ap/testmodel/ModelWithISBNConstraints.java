/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel;

import org.hibernate.validator.constraints.ISBN;

/**
 * @author Marko Bekhta
 */
public class ModelWithISBNConstraints {

	@ISBN
	private String string;

	@ISBN
	private CharSequence charSequence;

	@ISBN
	private Integer integer;

}
