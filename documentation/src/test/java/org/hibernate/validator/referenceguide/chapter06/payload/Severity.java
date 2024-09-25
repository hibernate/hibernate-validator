/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.referenceguide.chapter06.payload;

import jakarta.validation.Payload;

//tag::include[]
public class Severity {
	public interface Info extends Payload {
	}

	public interface Error extends Payload {
	}
}
//end::include[]
