/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.aspectj.validation.spi;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

/**
 * Interface that should be implemented by end users to provide configured
 * {@link ValidatorFactory}.
 *
 * @author Marko Bekhta
 */
public interface ValidatorFactoryProducer {

	/**
	 * @return configured {@link ValidatorFactory} that will be used to create
	 * 		{@link Validator} to perform executable validation.
	 */
	ValidatorFactory getConfiguredValidatorFactory();
}
