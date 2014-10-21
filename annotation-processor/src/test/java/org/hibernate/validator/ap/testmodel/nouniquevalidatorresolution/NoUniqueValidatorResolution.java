/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.testmodel.nouniquevalidatorresolution;

import java.util.Set;

public class NoUniqueValidatorResolution {

	/**
	 * Allowed, as there is one maximally specific validator.
	 */
	@Size
	public Set<?> set;

	/**
	 * Not allowed, as two maximally specific validators exist.
	 */
	@Size
	public SerializableCollection<?> serializableCollection;
}
