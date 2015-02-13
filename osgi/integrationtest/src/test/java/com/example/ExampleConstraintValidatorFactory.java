/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example;

import java.util.concurrent.atomic.AtomicInteger;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;

/**
 * A custom constraint validator factory, configured through META-INF/validation.xml.
 *
 * @author Gunnar Morling
 */
public class ExampleConstraintValidatorFactory implements ConstraintValidatorFactory {

	public static AtomicInteger invocationCounter = new AtomicInteger();

	@Override
	public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
		try {
			return key.newInstance();
		}
		catch ( InstantiationException | IllegalAccessException e ) {
			throw new RuntimeException( e );
		}
		finally {
			invocationCounter.incrementAndGet();
		}
	}

	@Override
	public void releaseInstance(ConstraintValidator<?, ?> instance) {
	}
}
