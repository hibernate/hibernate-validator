/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
//tag::include[]
package org.hibernate.validator.referenceguide.chapter09;

//end::include[]
import java.util.Locale;

import jakarta.validation.MessageInterpolator;

//tag::include[]
public class MyMessageInterpolator implements MessageInterpolator {

	@Override
	public String interpolate(String messageTemplate, Context context) {
		//...
		return null;
	}

	@Override
	public String interpolate(String messageTemplate, Context context, Locale locale) {
		//...
		return null;
	}
}
//end::include[]
