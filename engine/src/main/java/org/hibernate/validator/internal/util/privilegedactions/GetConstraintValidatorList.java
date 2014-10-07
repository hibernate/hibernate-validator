/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.AccessController;
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
public class GetConstraintValidatorList implements PrivilegedAction<List<ConstraintValidator<?, ?>>> {

	public static List<ConstraintValidator<?, ?>> getConstraintValidatorList() {
		final GetConstraintValidatorList action = new GetConstraintValidatorList();
		if ( System.getSecurityManager() != null ) {
			return AccessController.doPrivileged( action );
		}
		else {
			return action.run();
		}
	}

	public List<ConstraintValidator<?, ?>> run() {
		// Option #1: try first context class loader
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		List<ConstraintValidator<?, ?>> constraintValidatorList = loadConstraintValidators( classloader );

		// Option #2: if we cannot find any service files with the context class loader use the current class loader
		if ( constraintValidatorList.isEmpty() ) {
			classloader = GetConstraintValidatorList.class.getClassLoader();
			constraintValidatorList = loadConstraintValidators( classloader );
		}

		return constraintValidatorList;
	}

	private List<ConstraintValidator<?, ?>> loadConstraintValidators(ClassLoader classloader) {
		ServiceLoader<ConstraintValidator> loader = ServiceLoader.load( ConstraintValidator.class, classloader );
		Iterator<ConstraintValidator> constraintValidatorIterator = loader.iterator();
		List<ConstraintValidator<?, ?>> constraintValidators = new ArrayList<ConstraintValidator<?, ?>>();
		while ( constraintValidatorIterator.hasNext() ) {
			try {
				constraintValidators.add( constraintValidatorIterator.next() );
			}
			catch ( ServiceConfigurationError e ) {
				// ignore, because it can happen when multiple
				// services are present and some of them are not class loader
				// compatible with our API.
			}
		}
		return constraintValidators;
	}
}


