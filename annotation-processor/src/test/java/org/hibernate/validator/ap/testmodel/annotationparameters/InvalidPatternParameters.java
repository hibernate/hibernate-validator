/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import javax.validation.constraints.Pattern;

/**
 * @author Marko Bekhta
 */
public class InvalidPatternParameters {

	@Pattern(regexp = "\\")
	private String strings1;

	@Pattern(regexp = "[a")
	private String strings2;

	@Pattern(regexp = "+")
	private String strings3;

	@Pattern.List({ @Pattern(regexp = "+"), @Pattern(regexp = "[a") })
	private String strings4;

	public InvalidPatternParameters(
			@Pattern(regexp = "\\") String strings1,
			@Pattern(regexp = "[test") String strings2,
			@Pattern(regexp = "+") String strings3
	) {

	}

	public void doSomething(
			@Pattern(regexp = "\\") String strings1,
			@Pattern(regexp = "[test") String strings2,
			@Pattern(regexp = "+") String strings3
	) {

	}

	@Pattern(regexp = "\\")
	public String doSomething() {
		return "";
	}
}
