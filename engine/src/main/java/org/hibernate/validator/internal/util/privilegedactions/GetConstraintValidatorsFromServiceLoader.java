/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import javax.validation.ConstraintValidator;

/**
 * @author Hardy Ferentschik
 */
public class GetConstraintValidatorsFromServiceLoader implements PrivilegedAction<List<ConstraintValidator<?, ?>>> {

	private static final GetConstraintValidatorsFromServiceLoader INSTANCE = new GetConstraintValidatorsFromServiceLoader();

	private GetConstraintValidatorsFromServiceLoader() {
	}

	public static GetConstraintValidatorsFromServiceLoader action() {
		return INSTANCE;
	}

	@Override
	public List<ConstraintValidator<?, ?>> run() {
		// Option #1: try first context class loader
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		List<ConstraintValidator<?, ?>> constraintValidatorList = loadConstraintValidators( classloader );

		// Option #2: if we cannot find any service files with the context class loader use the current class loader
		if ( constraintValidatorList.isEmpty() ) {
			classloader = GetConstraintValidatorsFromServiceLoader.class.getClassLoader();
			constraintValidatorList = loadConstraintValidators( classloader );
		}

		return constraintValidatorList;
	}

	@SuppressWarnings("rawtypes")
	private List<ConstraintValidator<?, ?>> loadConstraintValidators(ClassLoader classloader) {
		ServiceLoader<ConstraintValidator> loader = ServiceLoader.load( ConstraintValidator.class, classloader );
		Iterator<ConstraintValidator> constraintValidatorIterator = loader.iterator();
		List<ConstraintValidator<?, ?>> constraintValidators = new ArrayList<ConstraintValidator<?, ?>>();
		while ( constraintValidatorIterator.hasNext() ) {
			try {
				constraintValidators.add( constraintValidatorIterator.next() );
			}
			catch (ServiceConfigurationError e) {
				// ignore, because it can happen when multiple
				// services are present and some of them are not class loader
				// compatible with our API.
			}
		}
		return constraintValidators;
	}
}
