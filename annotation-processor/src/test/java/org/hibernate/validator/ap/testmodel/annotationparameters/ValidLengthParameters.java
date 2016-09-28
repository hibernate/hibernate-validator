/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import org.hibernate.validator.constraints.Length;

public class ValidLengthParameters {

	@Length
	private String string1;

	@Length( min = 10 )
	private String string2;

	@Length( max = 10 )
	private String string3;

	@Length( min = 10, max = 15 )
	private String string4;

}
