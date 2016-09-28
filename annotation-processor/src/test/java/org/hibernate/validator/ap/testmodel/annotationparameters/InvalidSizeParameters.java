/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import javax.validation.constraints.Size;
import java.util.Collection;

public class InvalidSizeParameters {

	@Size( min = -1 )
	private Collection<String> strings1;

	@Size( max = -10 )
	private Collection<String> strings2;

	@Size( min = 15, max = 10 )
	private Collection<String> strings3;

}
