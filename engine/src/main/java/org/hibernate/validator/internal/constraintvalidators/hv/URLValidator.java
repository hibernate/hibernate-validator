/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.metadata.ConstraintDescriptor;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidator;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorInitializationContext;
import org.hibernate.validator.internal.util.RegexUtil;

/**
 * Validate that the character sequence (e.g. string) is a valid URL using the {@code java.net.URL} constructor.
 *
 * @author Hardy Ferentschik
 */
public class URLValidator implements HibernateConstraintValidator<org.hibernate.validator.constraints.URL, CharSequence> {
	private String protocol;
	private String host;
	private int port;
	private Pattern regexp;

	@Override
	public void initialize(ConstraintDescriptor<org.hibernate.validator.constraints.URL> descriptor, HibernateConstraintValidatorInitializationContext initializationContext) {
		org.hibernate.validator.constraints.URL url = descriptor.getAnnotation();
		this.protocol = url.protocol();
		this.host = url.host();
		this.port = url.port();
		this.regexp = RegexUtil.compile( url.regexp(), url.flags(), initializationContext );
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		if ( !regexp.matcher( value ).matches() ) {
			return false;
		}
		if ( value.length() == 0 ) {
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
