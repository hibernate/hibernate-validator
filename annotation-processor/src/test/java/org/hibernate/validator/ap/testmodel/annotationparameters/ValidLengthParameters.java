/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import org.hibernate.validator.constraints.Length;

/**
 * @author Marko Bekhta
 */
public class ValidLengthParameters {

	@Length
	private String string1;

	@Length(min = 10)
	private String string2;

	@Length(max = 10)
	private String string3;

	@Length(min = 10, max = 15)
	private String string4;

	@Length.List({ @Length(min = 10, max = 15), @Length(max = 10), @Length(min = 10), @Length })
	private String string5;

	public ValidLengthParameters(@Length(min = 10) String string) {

	}

	public void doSomething(@Length(min = 10) String string) {

	}

	@Length(min = 10)
	public String doSomething() {
		return "";
	}
}
