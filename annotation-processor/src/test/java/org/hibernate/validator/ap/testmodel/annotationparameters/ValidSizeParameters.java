/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import javax.validation.constraints.Size;
import java.util.Collection;

/**
 * @author Marko Bekhta
 */
public class ValidSizeParameters {

	@Size
	private Collection<String> strings1;

	@Size( min = 10 )
	private Collection<String> strings2;

	@Size( max = 10 )
	private Collection<String> strings3;

	@Size( min = 10, max = 15 )
	private Collection<String> strings4;

	@Size.List({ @Size, @Size(min = 10), @Size(max = 15), @Size(min = 10, max = 15) })
	private Collection<String> strings5;

}
