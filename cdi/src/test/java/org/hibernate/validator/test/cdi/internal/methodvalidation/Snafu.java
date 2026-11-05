package org.hibernate.validator.test.cdi.internal.methodvalidation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
public class Snafu {
	@ValidateOnExecution(type = { })
	@NotNull
	public String foo() {
		return null;
	}
}
