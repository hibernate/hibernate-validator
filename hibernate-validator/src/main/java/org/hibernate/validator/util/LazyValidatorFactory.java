/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.validation.Configuration;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.MessageInterpolator;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.ValidationProviderResolver;
import javax.validation.Validator;
import javax.validation.ValidatorContext;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;

/**
 * This class lazily initialize the ValidatorFactory on the first usage
 * One benefit is that no domain class is loaded until the ValidatorFactory is really needed.
 * Useful to avoid loading classes before JPA is initialized and has enhanced its classes.
 *
 * When no {@code Configuration} is passed, the provider is Hibernate Validator.
 *
 * This class is used by JBoss AS 6.
 *
 * Experimental, not considered a public API
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class LazyValidatorFactory implements ValidatorFactory {
	private final Configuration<?> configuration;
	private volatile ValidatorFactory delegate; //use as a barrier

	/**
	 * Use the default ValidatorFactory creation routine
	 */
	public LazyValidatorFactory() {
		this( null );
	}

	public LazyValidatorFactory(Configuration<?> configuration) {
		this.configuration = configuration;
	}

	private ValidatorFactory getDelegate() {
		ValidatorFactory result = delegate;
		if ( result == null ) {
			synchronized ( this ) {
				result = delegate;
				if ( result == null ) {
					delegate = result = initFactory();
				}
			}
		}
		return result;
	}

	public Validator getValidator() {
		return getDelegate().getValidator();
	}

	//we can initialize several times that's ok

	private ValidatorFactory initFactory() {
		if ( configuration == null ) {
			return Validation
					.byDefaultProvider()
					.providerResolver( new HibernateProviderResolver() )
					.configure()
					.buildValidatorFactory();
		}
		else {
			return configuration.buildValidatorFactory();
		}
	}

	public ValidatorContext usingContext() {
		return getDelegate().usingContext();
	}

	public MessageInterpolator getMessageInterpolator() {
		return getDelegate().getMessageInterpolator();
	}

	public TraversableResolver getTraversableResolver() {
		return getDelegate().getTraversableResolver();
	}

	public ConstraintValidatorFactory getConstraintValidatorFactory() {
		return getDelegate().getConstraintValidatorFactory();
	}

	public <T> T unwrap(Class<T> clazz) {
		return getDelegate().unwrap( clazz );
	}

	private static class HibernateProviderResolver implements ValidationProviderResolver {
		private final List<ValidationProvider<?>> providerList;

		private HibernateProviderResolver() {
			List<ValidationProvider<?>> customProviderList = new ArrayList<ValidationProvider<?>>();
			boolean containsHibernateValidator = false;
			ValidationProviderResolver defaultResolver = new DefaultValidationProviderResolver();
			for ( ValidationProvider<?> provider : defaultResolver.getValidationProviders() ) {
				if ( provider instanceof HibernateValidator ) {
					// make sure Hibernate Validator is the default Bean Validation implementation
					customProviderList.add( 0, provider );
					containsHibernateValidator = true;
				}
				else {
					customProviderList.add( provider );
				}

			}

			if ( !containsHibernateValidator ) {
				customProviderList.add( 0, new HibernateValidator() );
			}

			this.providerList = Collections.unmodifiableList( customProviderList );
		}

		public List<ValidationProvider<?>> getValidationProviders() {
			return providerList;
		}
	}

	// initially taken from javax.validation.Validation
	private static class DefaultValidationProviderResolver implements ValidationProviderResolver {
		//cache per classloader for an appropriate discovery
		//keep them in a weak hash map to avoid memory leaks and allow proper hot redeployment
		private static final Map<ClassLoader, List<ValidationProvider<?>>> providersPerClassloader = new WeakHashMap<ClassLoader, List<ValidationProvider<?>>>();
		private static final String SERVICES_FILE = "META-INF/services/" + ValidationProvider.class.getName();

		public List<ValidationProvider<?>> getValidationProviders() {
			ClassLoader classloader = GetClassLoader.fromContext();
			if ( classloader == null ) {
				classloader = GetClassLoader.fromClass( DefaultValidationProviderResolver.class );
			}

			List<ValidationProvider<?>> providers;
			synchronized ( providersPerClassloader ) {
				providers = providersPerClassloader.get( classloader );
			}

			if ( providers == null ) {
				List<String> providerNames = loadProviderNamesFromServiceFile( classloader );
				providers = instantiateProviders( providerNames );
			}

			synchronized ( providersPerClassloader ) {
				providersPerClassloader.put( classloader, providers );
			}

			return providers;
		}

		private List<ValidationProvider<?>> instantiateProviders(List<String> providerNames) {
			List<ValidationProvider<?>> providers = new ArrayList<ValidationProvider<?>>();
			Class<?> providerClass;
			for ( String providerName : providerNames ) {
				try {
					providerClass = loadClass( providerName, DefaultValidationProviderResolver.class );
				}
				catch ( ClassNotFoundException e ) {
					// ignore - we don't want to  fail the whole loading because of a black sheep. Hibernate Validator
					// will be added either way
					continue;
				}

				try {
					providers.add( (ValidationProvider) providerClass.newInstance() );
				}
				catch ( IllegalAccessException e ) {
					throw new ValidationException(
							"Unable to instantiate Bean Validation provider" + providerNames,
							e
					);
				}
				catch ( InstantiationException
						e ) {
					throw new ValidationException(
							"Unable to instantiate Bean Validation provider" + providerNames,
							e
					);
				}
			}
			return providers;
		}

		private List<String> loadProviderNamesFromServiceFile(ClassLoader classloader) {
			List<String> providerNames = new ArrayList<String>();
			try {
				Enumeration<URL> providerDefinitions = classloader.getResources( SERVICES_FILE );
				while ( providerDefinitions.hasMoreElements() ) {
					URL url = providerDefinitions.nextElement();
					InputStream stream = url.openStream();
					try {
						BufferedReader reader = new BufferedReader( new InputStreamReader( stream ), 100 );
						String name = reader.readLine();
						while ( name != null ) {
							name = name.trim();
							if ( !name.startsWith( "#" ) ) {
								providerNames.add( name );
							}
							name = reader.readLine();
						}
					}
					finally {
						stream.close();
					}
				}
			}
			catch ( IOException e ) {
				throw new ValidationException( "Unable to read " + SERVICES_FILE, e );
			}
			return providerNames;
		}

		private static Class<?> loadClass(String name, Class<?> caller) throws ClassNotFoundException {
			try {
				//try context classloader, if fails try caller classloader
				ClassLoader loader = GetClassLoader.fromContext();
				if ( loader != null ) {
					return loader.loadClass( name );
				}
			}
			catch ( ClassNotFoundException e ) {
				//trying caller classloader
				if ( caller == null ) {
					throw e;
				}
			}
			return Class.forName( name, true, GetClassLoader.fromClass( caller ) );
		}
	}

	private static class GetClassLoader implements PrivilegedAction<ClassLoader> {
		private final Class<?> clazz;

		public static ClassLoader fromContext() {
			final GetClassLoader action = new GetClassLoader( null );
			if ( System.getSecurityManager() != null ) {
				return AccessController.doPrivileged( action );
			}
			else {
				return action.run();
			}
		}

		public static ClassLoader fromClass(Class<?> clazz) {
			if ( clazz == null ) {
				throw new IllegalArgumentException( "Class is null" );
			}
			final GetClassLoader action = new GetClassLoader( clazz );
			if ( System.getSecurityManager() != null ) {
				return AccessController.doPrivileged( action );
			}
			else {
				return action.run();
			}
		}

		private GetClassLoader(Class<?> clazz) {
			this.clazz = clazz;
		}

		public ClassLoader run() {
			if ( clazz != null ) {
				return clazz.getClassLoader();
			}
			else {
				return Thread.currentThread().getContextClassLoader();
			}
		}
	}
}
