package org.hibernate.validator.test.cdi.internal.methodvalidation.inheritance;

import jakarta.validation.executable.ValidateOnExecution;

/**
 * @author Hardy Ferentschik
 */
@ValidateOnExecution
public class SimpleGreeter extends AbstractGreeter {
	@Override
	protected String getName() {
		return "John";
	}
}
