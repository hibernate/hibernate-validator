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
