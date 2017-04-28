/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.cdi;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;

/**
 * Finds {@link ValidationProvider} according to the default {@link ValidationProviderResolver} defined in the Bean
 * Validation specification. This implementation first uses thread's context classloader to locate providers. If no
 * suitable provider is found using the aforementioned class loader, it uses the current class loader.
 * <p>
 * This custom resolver is used here to circumvent the static caching of resolved providers within the default
 * implementation in {@link Validation}. While this cache will be cleaned up eventually, this doesn't happen right after
 * undeployment in servers such as WildFly which causes the affected memory to be retained longer than strictly
 * necessary.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class SimpleValidationProviderResolver implements ValidationProviderResolver {

	private final List<ValidationProvider<?>> validationProviders;

	SimpleValidationProviderResolver() {
		validationProviders = Collections.unmodifiableList( GetValidationProviderListAction.getValidationProviderList() );
	}

	@Override
	public List<ValidationProvider<?>> getValidationProviders() {
		return validationProviders;
	}

	private static class GetValidationProviderListAction implements PrivilegedAction<List<ValidationProvider<?>>> {

		public static List<ValidationProvider<?>> getValidationProviderList() {
			final GetValidationProviderListAction action = new GetValidationProviderListAction();
			if ( System.getSecurityManager() != null ) {
				return AccessController.doPrivileged( action );
			}
			else {
				return action.run();
			}
		}

		@Override
		public List<ValidationProvider<?>> run() {
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();

			// Option #1: try first context class loader
			List<ValidationProvider<?>> validationProviderList = loadProviders( classloader );

			// Option #2: if we cannot find any service files with the context class loader use the current class loader
			if ( validationProviderList.isEmpty() ) {
				classloader = SimpleValidationProviderResolver.class.getClassLoader();
				validationProviderList = loadProviders( classloader );
			}

			return validationProviderList;
		}

		private List<ValidationProvider<?>> loadProviders(ClassLoader classloader) {
			ServiceLoader<ValidationProvider> loader = ServiceLoader.load( ValidationProvider.class, classloader );
			Iterator<ValidationProvider> providerIterator = loader.iterator();
			List<ValidationProvider<?>> validationProviderList = new ArrayList<ValidationProvider<?>>();
			while ( providerIterator.hasNext() ) {
				try {
					validationProviderList.add( providerIterator.next() );
				}
				catch (ServiceConfigurationError e) {
					// ignore, because it can happen when multiple
					// providers are present and some of them are not class loader
					// compatible with our API.
				}
			}
			return validationProviderList;
		}
	}
}
