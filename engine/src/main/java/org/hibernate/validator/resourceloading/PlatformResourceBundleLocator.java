/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.resourceloading;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.jboss.logging.Logger;

import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

/**
 * A resource bundle locator, that loads resource bundles by simply
 * invoking <code>ResourceBundle.loadBundle(...)</code>.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class PlatformResourceBundleLocator implements ResourceBundleLocator {
	private static final Logger log = Logger.getLogger( PlatformResourceBundleLocator.class.getName() );
	private final String bundleName;

	public PlatformResourceBundleLocator(String bundleName) {
		this.bundleName = bundleName;
	}

	/**
	 * Search current thread classloader for the resource bundle. If not found,
	 * search validator (this) classloader.
	 *
	 * @param locale The locale of the bundle to load.
	 *
	 * @return the resource bundle or <code>null</code> if none is found.
	 */
	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		ResourceBundle rb = null;
		ClassLoader classLoader = GetClassLoader.fromContext();
		if ( classLoader != null ) {
			rb = loadBundle(
					classLoader, locale, bundleName
					+ " not found by thread local classloader"
			);
		}
		if ( rb == null ) {
			classLoader = GetClassLoader.fromClass( PlatformResourceBundleLocator.class );
			rb = loadBundle(
					classLoader, locale, bundleName
					+ " not found by validator classloader"
			);
		}
		if ( rb != null ) {
			log.debugf( "%s found.", bundleName );
		}
		else {
			log.debugf( "%s not found.", bundleName );
		}
		return rb;
	}

	private ResourceBundle loadBundle(ClassLoader classLoader, Locale locale, String message) {
		ResourceBundle rb = null;
		try {
			rb = ResourceBundle.getBundle(
					bundleName, locale,
					classLoader
			);
		}
		catch ( MissingResourceException e ) {
			log.trace( message );
		}
		return rb;
	}

	private static class GetClassLoader implements PrivilegedAction<ClassLoader> {
		private final Class<?> clazz;

		private static ClassLoader fromContext() {
			final GetClassLoader action = new GetClassLoader( null );
			if ( System.getSecurityManager() != null ) {
				return AccessController.doPrivileged( action );
			}
			else {
				return action.run();
			}
		}

		private static ClassLoader fromClass(Class<?> clazz) {
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

		@Override
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
