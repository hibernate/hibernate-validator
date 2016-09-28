/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collection;

public class ValidPatternParameters {

	@Pattern( regexp = "test" )
	private String strings1;

	@Pattern( regexp = "[test]" )
	private String strings2;

	@Pattern( regexp = "\\." )
	private String strings3;

}
