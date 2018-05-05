/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.aspectj.validation.internal;

import java.lang.invoke.MethodHandles;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.ServiceLoader;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import org.hibernate.validator.aspectj.validation.internal.util.logging.Log;
import org.hibernate.validator.aspectj.validation.internal.util.logging.LoggerFactory;
import org.hibernate.validator.aspectj.validation.internal.util.privilegedactions.GetValidatorProviderFactoryFromServiceLoader;
import org.hibernate.validator.aspectj.validation.spi.ValidatorFactoryProducer;

/**
 * Uses {@link ServiceLoader} mechanism to look up user provided {@link ValidatorFactoryProducer}.
 * If none are found will provide a default {@link ValidatorFactory}.
 *
 * @author Marko Bekhta
 */
class ServiceLoaderBasedValidatorFactoryProducer {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private final ValidatorFactory validatorFactory;

	ServiceLoaderBasedValidatorFactoryProducer() {
		List<ValidatorFactoryProducer> validatorFactoryProducers = run( GetValidatorProviderFactoryFromServiceLoader.action() );
		if ( validatorFactoryProducers.isEmpty() ) {
			validatorFactory = Validation.buildDefaultValidatorFactory();
			LOG.defaultValidatorUsage();
		}
		else {
			ValidatorFactoryProducer producer = validatorFactoryProducers.get( 0 );
			try {
				validatorFactory = producer.getConfiguredValidatorFactory();
				if ( validatorFactoryProducers.size() > 1 ) {
					LOG.multipleValidatorFactoryProducers( producer.getClass() );
				}
			}
			catch (Exception e) {
				throw LOG.errorCreatingValidatorFactoryFromProducer( producer.getClass(), e );
			}
		}
	}

	public ValidatorFactory getValidatorFactory() {
		return validatorFactory;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
