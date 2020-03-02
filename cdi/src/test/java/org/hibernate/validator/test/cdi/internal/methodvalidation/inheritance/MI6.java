/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import jakarta.validation.constraints.NotNull;

/**
 * @author Hardy Ferentschik
 */
public class MI6 extends SecretServiceBase {
	@Override
	public void whisper(@NotNull String secret) {
	}
}
