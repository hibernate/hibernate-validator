/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.xml;

import org.hibernate.validator.internal.engine.DefaultParameterNameProvider;

/**
 * @author Gunnar Morling
 */
public class CustomParameterNameProvider extends DefaultParameterNameProvider {

	@Override
	protected String getPrefix() {
		return "param";
	}

}
