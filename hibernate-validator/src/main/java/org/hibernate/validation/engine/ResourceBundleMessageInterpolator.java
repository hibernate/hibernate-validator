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
	private ResourceBundle defaultResourceBundle;
	private ResourceBundle userResourceBundle;

	public ResourceBundleMessageInterpolator() {
		userResourceBundle = getFileBasedResourceBundle();
		defaultResourceBundle = ResourceBundle.getBundle( DEFAULT_VALIDATION_MESSAGES );
	}

	public ResourceBundleMessageInterpolator(ResourceBundle resourceBundle) {
		if ( resourceBundle == null ) {
			userResourceBundle = getFileBasedResourceBundle();
		}
		else {
			this.userResourceBundle = resourceBundle;
		}
		defaultResourceBundle = ResourceBundle.getBundle( DEFAULT_VALIDATION_MESSAGES );
	}

	public String interpolate(String message, ConstraintDescriptor constraintDescriptor, Object value) {
		// probably no need for caching, but it could be done by parameters since the map
		// is immutable and uniquely built per Validation definition, the comparaison has to be based on == and not equals though
		return replace( message, constraintDescriptor.getParameters() );
	}

	public String interpolate(String message, ConstraintDescriptor constraintDescriptor, Object value, Locale locale) {
		throw new UnsupportedOperationException( "Interpolation for Locale. Has to be done." );
	}

	/**
	 * Search current thread classloader for the resource bundle. If not found, search validator (this) classloader.
	 *
	 * @return the resource bundle or <code>null</code> if none is found.
	 */
	private ResourceBundle getFileBasedResourceBundle() {
		ResourceBundle rb = null;
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if ( classLoader != null ) {
			rb = loadBundle( classLoader, USER_VALIDATION_MESSAGES + " not found by thread local classloader" );
		}
		if ( rb == null ) {
			rb = loadBundle(
					this.getClass().getClassLoader(), USER_VALIDATION_MESSAGES + " not found by validator classloader"
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

	private ResourceBundle loadBundle(ClassLoader classLoader, String message) {
		ResourceBundle rb = null;
		try {
			rb = ResourceBundle.getBundle( USER_VALIDATION_MESSAGES, Locale.getDefault(), classLoader );
		}
		catch ( MissingResourceException e ) {
			log.trace( message );
		}
		return rb;
	}

	private String replace(String message, Map<String, Object> parameters) {
		Matcher matcher = messagePattern.matcher( message );
		StringBuffer sb = new StringBuffer();
		while ( matcher.find() ) {
			matcher.appendReplacement( sb, resolveParameter( matcher.group( 1 ), parameters ) );
		}
		matcher.appendTail( sb );
		return sb.toString();
	}

	private String resolveParameter(String token, Map<String, Object> parameters) {
		Object variable = parameters.get( token );
		if ( variable != null ) {
			return variable.toString();
		}

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
			buffer.append( replace( string, parameters ) );
		}
		return buffer.toString();
	}
}
