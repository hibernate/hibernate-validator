package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ExecutableType;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
public interface Encryptor {
	@ValidateOnExecution(type = ExecutableType.NONE)
	@NotNull
	String encrypt(String secret);
}
