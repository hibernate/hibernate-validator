package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
@ValidateOnExecution
public class RefusingEncryptor implements Encryptor {
	@Override
	public String encrypt(String secret) {
		return null;
	}
}
