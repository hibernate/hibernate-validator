/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.annotationparameters;

import java.util.Collection;
import java.util.Collections;
import javax.validation.constraints.Size;

/**
 * @author Marko Bekhta
 */
public class ValidSizeParameters {

	@Size
	private Collection<String> strings1;

	@Size(min = 10)
	private Collection<String> strings2;

	@Size(max = 10)
	private Collection<String> strings3;

	@Size(min = 10, max = 15)
	private Collection<String> strings4;

	@Size.List({ @Size, @Size(min = 10), @Size(max = 15), @Size(min = 10, max = 15) })
	private Collection<String> strings5;

	public ValidSizeParameters(@Size(min = 10) Collection<String> string) {

	}

	public void doSomething(@Size(min = 10) Collection<String> string) {

	}

	@Size(min = 10)
	public Collection<String> doSomething() {
		return Collections.emptyList();
	}

}
