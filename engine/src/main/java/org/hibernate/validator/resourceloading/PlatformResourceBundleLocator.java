/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.resourceloading;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.actions.GetClassLoader;
import org.hibernate.validator.internal.util.actions.GetMethod;
import org.hibernate.validator.internal.util.actions.GetResources;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.stereotypes.Immutable;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;

/**
 * A resource bundle locator, that loads resource bundles by invoking {@code ResourceBundle.loadBundle(String, Local, ClassLoader)}.
 * <p>
 * This locator is also able to load all property files of a given name (in case there are multiple with the same
 * name on the classpath) and aggregates them into a {@code ResourceBundle}.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Guillaume Smet
 */
public class PlatformResourceBundleLocator implements ResourceBundleLocator {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );
	private static final boolean RESOURCE_BUNDLE_CONTROL_INSTANTIABLE = determineAvailabilityOfResourceBundleControl();

	private final String bundleName;
	private final ClassLoader classLoader;
	private final boolean aggregate;

	@Immutable
	private final Map<Locale, ResourceBundle> preloadedResourceBundles;

	/**
	 * Creates a new {@link PlatformResourceBundleLocator}.
	 *
	 * @param bundleName the name of the bundle to load
	 */
	public PlatformResourceBundleLocator(String bundleName) {
		this( bundleName, Collections.emptySet() );
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
		this( bundleName, Collections.emptySet(), classLoader );
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
		this( bundleName, Collections.emptySet(), classLoader, aggregate );
	}

	/**
	 * Creates a new {@link PlatformResourceBundleLocator}.
	 *
	 * @param bundleName the name of the bundle to load
	 * @param localesToInitialize the set of locales to initialize at bootstrap
	 *
	 * @since 6.1.1
	 */
	@Incubating
	public PlatformResourceBundleLocator(String bundleName, Set<Locale> localesToInitialize) {
		this( bundleName, localesToInitialize, null );
	}

	/**
	 * Creates a new {@link PlatformResourceBundleLocator}.
	 *
	 * @param bundleName the name of the bundle to load
	 * @param localesToInitialize the set of locales to initialize at bootstrap
	 * @param classLoader the classloader to be used for loading the bundle. If {@code null}, the current thread context
	 * classloader and finally Hibernate Validator's own classloader will be used for loading the specified
	 * bundle.
	 *
	 * @since 6.1.1
	 */
	@Incubating
	public PlatformResourceBundleLocator(String bundleName, Set<Locale> localesToInitialize, ClassLoader classLoader) {
		this( bundleName, localesToInitialize, classLoader, false );
	}

	/**
	 * Creates a new {@link PlatformResourceBundleLocator}.
	 *
	 * @param bundleName the name of the bundle to load
	 * @param localesToInitialize the set of locales to initialize at bootstrap
	 * @param classLoader the classloader to be used for loading the bundle. If {@code null}, the current thread context
	 * classloader and finally Hibernate Validator's own classloader will be used for loading the specified
	 * bundle.
	 * @param aggregate Whether or not all resource bundles of a given name should be loaded and potentially merged.
	 *
	 * @since 6.1
	 */
	@Incubating
	public PlatformResourceBundleLocator(String bundleName,
			Set<Locale> localesToInitialize,
			ClassLoader classLoader,
			boolean aggregate) {
		Contracts.assertNotNull( bundleName, "bundleName" );

		this.bundleName = bundleName;
		this.classLoader = classLoader;

		this.aggregate = aggregate && RESOURCE_BUNDLE_CONTROL_INSTANTIABLE;

		if ( !localesToInitialize.isEmpty() ) {
			Map<Locale, ResourceBundle> tmpPreloadedResourceBundles = CollectionHelper.newHashMap( localesToInitialize.size() );
			for ( Locale localeToPreload : localesToInitialize ) {
				tmpPreloadedResourceBundles.put( localeToPreload, doGetResourceBundle( localeToPreload ) );
			}
			this.preloadedResourceBundles = CollectionHelper.toImmutableMap( tmpPreloadedResourceBundles );
		}
		else {
			this.preloadedResourceBundles = Collections.emptyMap();
		}
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
		if ( !preloadedResourceBundles.isEmpty() ) {
			// we need to use containsKey() as the cached resource bundle can be null
			if ( preloadedResourceBundles.containsKey( locale ) ) {
				return preloadedResourceBundles.get( locale );
			}
			else {
				throw LOG.uninitializedLocale( locale );
			}
		}

		return doGetResourceBundle( locale );
	}

	private ResourceBundle doGetResourceBundle(Locale locale) {
		ResourceBundle rb = null;

		if ( classLoader != null ) {
			rb = loadBundle(
					classLoader, locale, bundleName
							+ " not found by user-provided classloader"
			);
		}

		if ( rb == null ) {
			ClassLoader classLoader = GetClassLoader.fromContext();
			if ( classLoader != null ) {
				rb = loadBundle(
						classLoader, locale, bundleName
								+ " not found by thread context classloader"
				);
			}
		}

		if ( rb == null ) {
			ClassLoader classLoader = GetClassLoader.fromClass( PlatformResourceBundleLocator.class );
			rb = loadBundle(
					classLoader, locale, bundleName
							+ " not found by validator classloader"
			);
		}
		if ( rb != null ) {
			LOG.debugf( "%s found.", bundleName );
		}
		else {
			LOG.debugf( "%s not found.", bundleName );
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
						AggregateResourceBundleControl.CONTROL
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
		catch (MissingResourceException e) {
			LOG.trace( message );
		}
		return rb;
	}

	/**
	 * Check whether ResourceBundle.Control is available, which is needed for bundle aggregation. If not, we'll skip
	 * resource aggregation.
	 * <p>
	 * It is *not* available
	 * <ul>
	 * <li>in the Google App Engine environment</li>
	 * <li>when running HV as Java 9 named module (which would be the case when adding a module-info descriptor to the
	 * HV JAR)</li>
	 * </ul>
	 *
	 * @see <a href="http://code.google.com/appengine/docs/java/jrewhitelist.html">GAE JRE whitelist</a>
	 * @see <a href="https://hibernate.atlassian.net/browse/HV-1023">HV-1023</a>
	 * @see <a href="http://download.java.net/java/jdk9/docs/api/java/util/ResourceBundle.Control.html">ResourceBundle.Control</a>
	 */
	private static boolean determineAvailabilityOfResourceBundleControl() {
		try {
			ResourceBundle.Control dummyControl = AggregateResourceBundleControl.CONTROL;

			if ( dummyControl == null ) {
				return false;
			}

			Method getModule = GetMethod.action( Class.class, "getModule" );
			// not on Java 9
			if ( getModule == null ) {
				return true;
			}

			// on Java 9, check whether HV is a named module
			Object module = getModule.invoke( PlatformResourceBundleLocator.class );
			Method isNamedMethod = GetMethod.action( module.getClass(), "isNamed" );
			boolean isNamed = (Boolean) isNamedMethod.invoke( module );

			return !isNamed;
		}
		catch (Throwable e) {
			LOG.info( MESSAGES.unableToUseResourceBundleAggregation() );
			return false;
		}
	}

	private static class AggregateResourceBundleControl extends ResourceBundle.Control {

		private static final ResourceBundle.Control CONTROL = new AggregateResourceBundleControl();

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
			List<ResourceBundle> resourceBundles = load( resourceName, loader );
			return resourceBundles.isEmpty() ? null : new AggregateResourceBundle( resourceBundles );
		}

		private static List<ResourceBundle> load(String resourceName, ClassLoader loader) throws IOException {
			List<ResourceBundle> resourceBundles = new ArrayList<>();

			Enumeration<URL> urls = GetResources.action( loader, resourceName );
			while ( urls.hasMoreElements() ) {
				URL url = urls.nextElement();
				try ( InputStream propertyStream = url.openStream() ) {
					resourceBundles.add( new PropertyResourceBundle( propertyStream ) );
				}
			}

			return resourceBundles;
		}
	}
}
