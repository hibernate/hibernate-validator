/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validate that the character sequence (e.g. string) is a valid URL using the {@code java.net.URL} constructor.
 *
 * @author Hardy Ferentschik
 */
public class URLValidator implements ConstraintValidator<org.hibernate.validator.constraints.URL, CharSequence> {
	private String protocol;
	private String host;
	private int port;

	@Override
	public void initialize(org.hibernate.validator.constraints.URL url) {
		this.protocol = url.protocol();
		this.host = url.host();
		this.port = url.port();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null || value.length() == 0 ) {
			return true;
		}

		java.net.URL url;
		try {
			url = new java.net.URI( value.toString() ).toURL();
		}
		catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
			return false;
		}

		if ( protocol != null && protocol.length() > 0 && !url.getProtocol().equals( protocol ) ) {
			return false;
		}

		if ( host != null && host.length() > 0 && !url.getHost().equals( host ) ) {
			return false;
		}

		if ( port != -1 && url.getPort() != port ) {
			return false;
		}

		return true;
	}
}
