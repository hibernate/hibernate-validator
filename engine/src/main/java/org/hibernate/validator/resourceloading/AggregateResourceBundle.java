/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.resourceloading;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * Inspired by <a href="http://stackoverflow.com/questions/4614465/is-it-possible-to-include-resource-bundle-files-within-a-resource-bundle">this</a>
 * Stack Overflow question.
 *
 * @author Hardy Ferentschik
 */
public class AggregateResourceBundle extends ResourceBundle {

	protected static final Control CONTROL = new AggregateResourceBundleControl();
	private Properties properties;

	public AggregateResourceBundle(String baseName) {
		setParent( ResourceBundle.getBundle( baseName, CONTROL ) );
	}

	protected AggregateResourceBundle(Properties properties) {
		this.properties = properties;
	}

	@Override
	protected Object handleGetObject(String key) {
		return properties != null ? properties.get( key ) : parent.getObject( key );
	}

	@Override
	@SuppressWarnings("unchecked")
	public Enumeration<String> getKeys() {
		return properties != null ? (Enumeration<String>) properties.propertyNames() : parent.getKeys();
	}

	protected static class AggregateResourceBundleControl extends Control {
		@Override
		public ResourceBundle newBundle(
				String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
				throws IllegalAccessException, InstantiationException, IOException {
			Properties properties = load( baseName, loader );
			return new AggregateResourceBundle( properties );
		}

		private Properties load(String baseName, ClassLoader loader) throws IOException {
			Properties aggregatedProperties = new Properties();
			Enumeration<URL> urls = loader.getResources( baseName + ".properties" );
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


