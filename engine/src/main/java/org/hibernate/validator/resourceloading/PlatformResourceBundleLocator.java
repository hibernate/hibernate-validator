/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.resourceloading;

import java.io.IOException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;
import org.hibernate.validator.internal.util.privilegedactions.GetResources;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.jboss.logging.Logger;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * A resource bundle locator, that loads resource bundles by invoking {@code ResourceBundle.loadBundle(String, Local, ClassLoader)}.
 * <p>
 * This locator is also able to load all property files of a given name (in case there are multiple with the same
 * name on the classpath) and aggregates them into a {@code ResourceBundle}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class PlatformResourceBundleLocator implements ResourceBundleLocator {

	private static final Logger log = Logger.getLogger( PlatformResourceBundleLocator.class.getName() );
	private static final boolean RESOURCE_BUNDLE_CONTROL_INSTANTIABLE = determineAvailabilityOfResourceBundleControl();

	private final String bundleName;
	private final ClassLoader classLoader;
	private final boolean aggregate;

	public PlatformResourceBundleLocator(String bundleName) {
		this( bundleName, null );
	}

	/**
	 * Creates a new {@link PlatformResourceBundleLocator}.
	 *
	 * @param bundleName the name of the bundle to load
	 * @param classLoader the classloader to be used for loading the bundle. If {@code null}, the current thread context
	 * classloader and finally Hibernate Validator's own classloader will be used for loading the specified
	 * bundle.
	 *
	 * @since 5.2
	 */
	public PlatformResourceBundleLocator(String bundleName, ClassLoader classLoader) {
		this( bundleName, classLoader, false );
	}

	/**
	 * Creates a new {@link PlatformResourceBundleLocator}.
	 *
	 * @param bundleName the name of the bundle to load
	 * @param classLoader the classloader to be used for loading the bundle. If {@code null}, the current thread context
	 * classloader and finally Hibernate Validator's own classloader will be used for loading the specified
	 * bundle.
	 * @param aggregate Whether or not all resource bundles of a given name should be loaded and potentially merged.
	 *
	 * @since 5.2
	 */
	public PlatformResourceBundleLocator(String bundleName, ClassLoader classLoader, boolean aggregate) {
		Contracts.assertNotNull( bundleName, "bundleName" );

		this.bundleName = bundleName;
		this.classLoader = classLoader;

		this.aggregate = aggregate && RESOURCE_BUNDLE_CONTROL_INSTANTIABLE;
	}

	/**
	 * Search current thread classloader for the resource bundle. If not found,
	 * search validator (this) classloader.
	 *
	 * @param locale The locale of the bundle to load.
	 *
	 * @return the resource bundle or {@code null} if none is found.
	 */
	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		ResourceBundle rb = null;

		if ( classLoader != null ) {
			rb = loadBundle(
					classLoader, locale, bundleName
							+ " not found by user-provided classloader"
			);
		}

		if ( rb == null ) {
			ClassLoader classLoader = run( GetClassLoader.fromContext() );
			if ( classLoader != null ) {
				rb = loadBundle(
						classLoader, locale, bundleName
								+ " not found by thread context classloader"
				);
			}
		}

		if ( rb == null ) {
			ClassLoader classLoader = run( GetClassLoader.fromClass( PlatformResourceBundleLocator.class ) );
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
			if ( aggregate ) {
				rb = ResourceBundle.getBundle(
						bundleName,
						locale,
						classLoader,
						AggregateResourceBundle.CONTROL
				);
			}
			else {
				rb = ResourceBundle.getBundle(
						bundleName,
						locale,
						classLoader
				);
			}
		}
		catch ( MissingResourceException e ) {
			log.trace( message );
		}
		return rb;
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

	/**
	 *
	 * In an Google App Engine environment bundle aggregation is not possible, since ResourceBundle.Control
	 * is not on the list of white listed classes in this environment.
	 * to create AggregateResourceBundle.CONTROL proactively, if it fails skip resource aggregation.
	 *
	 * @see <a href="http://code.google.com/appengine/docs/java/jrewhitelist.html">JRE whitelist</a>
	 * @see <a href="https://hibernate.atlassian.net/browse/HV-1023">HV-1023</a>
	 */
	private static boolean determineAvailabilityOfResourceBundleControl() {
		try {
			@SuppressWarnings("unused")
			ResourceBundle.Control dummyControl = AggregateResourceBundle.CONTROL;
			return true;
		}
		catch ( NoClassDefFoundError e ) {
			log.info( MESSAGES.unableToUseResourceBundleAggregation() );
			return false;
		}
	}

	/**
	 * Inspired by <a href="http://stackoverflow.com/questions/4614465/is-it-possible-to-include-resource-bundle-files-within-a-resource-bundle">this</a>
	 * Stack Overflow question.
	 */
	private static class AggregateResourceBundle extends ResourceBundle {

		protected static final Control CONTROL = new AggregateResourceBundleControl();
		private final Properties properties;

		protected AggregateResourceBundle(Properties properties) {
			this.properties = properties;
		}

		@Override
		protected Object handleGetObject(String key) {
			return properties.get( key );
		}

		@Override
		public Enumeration<String> getKeys() {
			Set<String> keySet = newHashSet();
			keySet.addAll( properties.stringPropertyNames() );
			if ( parent != null ) {
				keySet.addAll( Collections.list( parent.getKeys() ) );
			}
			return Collections.enumeration( keySet );
		}
	}

	private static class AggregateResourceBundleControl extends ResourceBundle.Control {
		@Override
		public ResourceBundle newBundle(
				String baseName,
				Locale locale,
				String format,
				ClassLoader loader,
				boolean reload)
				throws IllegalAccessException, InstantiationException, IOException {
			// only *.properties files can be aggregated. Other formats are delegated to the default implementation
			if ( !"java.properties".equals( format ) ) {
				return super.newBundle( baseName, locale, format, loader, reload );
			}

			String resourceName = toBundleName( baseName, locale ) + ".properties";
			Properties properties = load( resourceName, loader );
			return properties.size() == 0 ? null : new AggregateResourceBundle( properties );
		}

		private Properties load(String resourceName, ClassLoader loader) throws IOException {
			Properties aggregatedProperties = new Properties();
			Enumeration<URL> urls = run( GetResources.action( loader, resourceName ) );
			while ( urls.hasMoreElements() ) {
				URL url = urls.nextElement();
				Properties properties = new Properties();
				properties.load( url.openStream() );
				aggregatedProperties.putAll( properties );
			}
			return aggregatedProperties;
		}
	}
}
