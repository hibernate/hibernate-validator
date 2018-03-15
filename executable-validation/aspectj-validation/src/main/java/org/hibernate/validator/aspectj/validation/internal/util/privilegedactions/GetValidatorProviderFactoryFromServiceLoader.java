/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.aspectj.validation.internal.util.privilegedactions;

import java.security.PrivilegedAction;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.hibernate.validator.aspectj.validation.spi.ValidatorFactoryProducer;

/**
 * A privileged action that accesses {@link ServiceLoader} mechanism to lookup
 * a list of {@link ValidatorFactoryProducer}.
 *
 * @author Marko Bekhta
 */
public class GetValidatorProviderFactoryFromServiceLoader implements PrivilegedAction<List<ValidatorFactoryProducer>> {

	private GetValidatorProviderFactoryFromServiceLoader() {
	}

	public static GetValidatorProviderFactoryFromServiceLoader action() {
		return new GetValidatorProviderFactoryFromServiceLoader();
	}

	@Override
	public List<ValidatorFactoryProducer> run() {
		return loadInstances( GetValidatorProviderFactoryFromServiceLoader.class.getClassLoader() );

	}

	private List<ValidatorFactoryProducer> loadInstances(ClassLoader classloader) {
		ServiceLoader<ValidatorFactoryProducer> loader = ServiceLoader.load( ValidatorFactoryProducer.class, classloader );

		Iterable<ValidatorFactoryProducer> iterable = () -> loader.iterator();
		return StreamSupport.stream( iterable.spliterator(), false )
				.collect( Collectors.toList() );
	}
}
