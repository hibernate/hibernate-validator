// $Id: DefaultValidationProviderResolver.java 114 2008-10-01 13:44:26Z hardy.ferentschik $
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package javax.validation.bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.validation.ValidationException;
import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;

/**
 * Find <code>ValidationProvider</code> according to the default <code>ValidationProviderResolver</code> defined in the
 * Bean Validation specification. This implementation uses the current classloader or the classloader which has loaded
 * the current class if the current class loader is unavailable. The classloader is used to retrieve the Service Provider files.
 * <p>
 * This class implements the Service Provider pattern described <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">here</a>.
 * Since we cannot rely on Java 6 we have to reimplement the <code>Service</code> functionality.
 * </p>
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class DefaultValidationProviderResolver implements ValidationProviderResolver {

	//cache per classloader for an appropriate discovery
	//keep them in a weak hashmap to avoid memory leaks and allow proper hot redeployment
	//TODO use a WeakConcurrentHashMap
	private static final Map<ClassLoader, List<ValidationProvider>> providersPerClassloader =
			new WeakHashMap<ClassLoader, List<ValidationProvider>>();

	private static final String SERVICES_FILE = "META-INF/services/" + ValidationProvider.class.getName();

	public List<ValidationProvider> getValidationProviders() {
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		if ( classloader == null ) {
			classloader = DefaultValidationProviderResolver.class.getClassLoader();
		}

		List<ValidationProvider> providers;
		synchronized ( providersPerClassloader ) {
			providers = providersPerClassloader.get( classloader );
		}

		if ( providers == null ) {
			providers = new ArrayList<ValidationProvider>();
			String name = null;
			try {
				Enumeration<URL> providerDefinitions = classloader.getResources( SERVICES_FILE );
				while ( providerDefinitions.hasMoreElements() ) {
					URL url = providerDefinitions.nextElement();
					InputStream stream = url.openStream();
					try {
						BufferedReader reader = new BufferedReader( new InputStreamReader( stream ), 100 );
						name = reader.readLine();
						while ( name != null ) {
							name = name.trim();
							if ( !name.startsWith( "#" ) ) {
								final Class<?> providerClass = loadClass(
										name,
										DefaultValidationProviderResolver.class
								);

								providers.add(
										( ValidationProvider ) providerClass.newInstance()
								);
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
			catch ( ClassNotFoundException e ) {
				//TODO is it better to not fail the whole loading because of a black sheep?
				throw new ValidationException( "Unable to load Bean Validation provider " + name, e );
			}
			catch ( IllegalAccessException e ) {
				throw new ValidationException( "Unable to instanciate Bean Validation provider" + name, e );
			}
			catch ( InstantiationException e ) {
				throw new ValidationException( "Unable to instanciate Bean Validation provider" + name, e );
			}

			synchronized ( providersPerClassloader ) {
				providersPerClassloader.put( classloader, providers );
			}
		}

		return providers;
	}

	public static Class<?> loadClass(String name, Class caller) throws ClassNotFoundException {
		try {
			//try context classloader, if fails try caller classloader
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
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
		return Class.forName( name, true, caller.getClassLoader() );
	}
}
