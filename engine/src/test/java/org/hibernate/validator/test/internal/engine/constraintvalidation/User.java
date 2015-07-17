/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.constraintvalidation;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Gunnar Morling
 */
@SuppressWarnings("unused")
public class User {

	private String firstName;

	@NotNull
	private String lastName;

	private String middleName;

	@Size(min = 5, max = 50)
	private String address1;

	private String address2;
}
