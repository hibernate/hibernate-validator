// $Id$
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
package org.hibernate.validation.engine;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ConstraintDescriptor;
import javax.validation.MessageInterpolator;

import org.slf4j.Logger;

import org.hibernate.validation.util.LoggerFactory;

/**
 * Resource bundle backed message interpolator.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public class ResourceBundleMessageInterpolator implements MessageInterpolator {
	private static final String DEFAULT_VALIDATION_MESSAGES = "org.hibernate.validation.ValidationMessages";
	private static final String USER_VALIDATION_MESSAGES = "ValidationMessages";
	private static final Logger log = LoggerFactory.make();

	/**
	 * Regular expression used to do message interpolation.
	 */
	private static final Pattern messagePattern = Pattern.compile( "\\{([\\w\\.]+)\\}" );

	/**
	 * The default locale for the current user.
	 */
	private final Locale defaultLocale;

	private final Map<Locale, ResourceBundle> userBundlesMap = new HashMap<Locale, ResourceBundle>();

	private final Map<Locale, ResourceBundle> defaultBundlesMap = new HashMap<Locale, ResourceBundle>();

	public ResourceBundleMessageInterpolator() {
		this( null );
	}

	public ResourceBundleMessageInterpolator(ResourceBundle resourceBundle) {

		defaultLocale = Locale.getDefault();

		if ( resourceBundle == null ) {
			ResourceBundle bundle = getFileBasedResourceBundle( defaultLocale );
			userBundlesMap.put( defaultLocale, bundle );

		}
		else {
			userBundlesMap.put( defaultLocale, resourceBundle );
		}

		defaultBundlesMap.put( defaultLocale, ResourceBundle.getBundle( DEFAULT_VALIDATION_MESSAGES, defaultLocale ) );
	}

	public String interpolate(String message, ConstraintDescriptor constraintDescriptor, Object value) {
		// probably no need for caching, but it could be done by parameters since the map
		// is immutable and uniquely built per Validation definition, the comparaison has to be based on == and not equals though
		return replace( message, constraintDescriptor.getParameters(), defaultLocale );
	}

	public String interpolate(String message, ConstraintDescriptor constraintDescriptor, Object value, Locale locale) {
		return replace( message, constraintDescriptor.getParameters(), locale );
	}

	/**
	 * Search current thread classloader for the resource bundle. If not found, search validator (this) classloader.
	 *
	 * @param locale The locale of the bundle to load.
	 *
	 * @return the resource bundle or <code>null</code> if none is found.
	 */
	private ResourceBundle getFileBasedResourceBundle(Locale locale) {
		ResourceBundle rb = null;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if ( classLoader != null ) {
			rb = loadBundle( classLoader, locale, USER_VALIDATION_MESSAGES + " not found by thread local classloader" );
		}
		if ( rb == null ) {
			rb = loadBundle(
					this.getClass().getClassLoader(),
					locale,
					USER_VALIDATION_MESSAGES + " not found by validator classloader"
			);
		}
		if ( log.isDebugEnabled() ) {
			if ( rb != null ) {
				log.debug( USER_VALIDATION_MESSAGES + " found" );
			}
			else {
				log.debug( USER_VALIDATION_MESSAGES + " not found. Delegating to " + DEFAULT_VALIDATION_MESSAGES );
			}
		}
		return rb;
	}

	private ResourceBundle loadBundle(ClassLoader classLoader, Locale locale, String message) {
		ResourceBundle rb = null;
		try {
			rb = ResourceBundle.getBundle( USER_VALIDATION_MESSAGES, locale, classLoader );
		}
		catch ( MissingResourceException e ) {
			log.trace( message );
		}
		return rb;
	}

	private String replace(String message, Map<String, Object> parameters, Locale locale) {
		Matcher matcher = messagePattern.matcher( message );
		StringBuffer sb = new StringBuffer();
		while ( matcher.find() ) {
			matcher.appendReplacement( sb, resolveParameter( matcher.group( 1 ), parameters, locale ) );
		}
		matcher.appendTail( sb );
		return sb.toString();
	}

	private String resolveParameter(String token, Map<String, Object> parameters, Locale locale) {
		Object variable = parameters.get( token );
		if ( variable != null ) {
			return variable.toString();
		}

		ResourceBundle userResourceBundle = findUserResourceBundle( locale );
		ResourceBundle defaultResourceBundle = findDefaultResourceBundle( locale );

		StringBuffer buffer = new StringBuffer();
		String string = null;
		try {
			string = userResourceBundle != null ? userResourceBundle.getString( token ) : null;
		}
		catch ( MissingResourceException e ) {
			//give a second chance with the default resource bundle
		}
		if ( string == null ) {
			try {
				string = defaultResourceBundle.getString( token );
			}
			catch ( MissingResourceException e ) {
				//return the unchanged string
				buffer.append( "{" ).append( token ).append( '}' );
			}
		}
		if ( string != null ) {
			// call resolve recusively!
			buffer.append( replace( string, parameters, locale ) );
		}
		return buffer.toString();
	}

	private ResourceBundle findDefaultResourceBundle(Locale locale) {
		if ( defaultBundlesMap.containsKey( locale ) ) {
			return defaultBundlesMap.get( locale );
		}

		ResourceBundle bundle = ResourceBundle.getBundle( DEFAULT_VALIDATION_MESSAGES, locale );
		defaultBundlesMap.put( locale, bundle );
		return bundle;
	}

	private ResourceBundle findUserResourceBundle(Locale locale) {
		if ( userBundlesMap.containsKey( locale ) ) {
			return userBundlesMap.get( locale );
		}

		ResourceBundle bundle = getFileBasedResourceBundle( locale );
		userBundlesMap.put( locale, bundle );
		return bundle;
	}
}
