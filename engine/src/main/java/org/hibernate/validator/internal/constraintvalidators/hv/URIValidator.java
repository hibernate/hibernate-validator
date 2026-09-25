/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.net.URISyntaxException;
import java.util.Locale;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.URI;

/**
 * Validates that the annotated character sequence is a valid URI according to RFC 3986.
 *
 * @author Andrea Boriero
 */
public class URIValidator implements ConstraintValidator<URI, CharSequence> {

	private URI.Type type;
	private String[] schemes;
	private URI.AuthorityRequirement authority;
	private boolean allowOpaque;

	@Override
	public void initialize(URI constraintAnnotation) {
		this.type = constraintAnnotation.type();
		this.schemes = constraintAnnotation.schemes();
		this.authority = constraintAnnotation.authority();
		this.allowOpaque = constraintAnnotation.allowOpaque();

		// Validate configuration: type=RELATIVE with allowOpaque=false is contradictory
		if ( type == URI.Type.RELATIVE && !allowOpaque ) {
			throw new IllegalArgumentException(
					"Invalid @URI configuration: type=RELATIVE with allowOpaque=false is contradictory " +
							"(relative URIs cannot be opaque)"
			);
		}

		// Normalize schemes to lowercase for case-insensitive matching
		if ( schemes != null && schemes.length > 0 ) {
			for ( int i = 0; i < schemes.length; i++ ) {
				schemes[i] = schemes[i].toLowerCase( Locale.ROOT );
			}
		}
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}

		// Parse URI using java.net.URI
		java.net.URI uri;
		try {
			uri = new java.net.URI( value.toString() );
		}
		catch (URISyntaxException e) {
			return false;
		}

		// Check type (absolute vs relative)
		if ( !isValidType( uri ) ) {
			return false;
		}

		// Check scheme restrictions (only for absolute URIs)
		if ( uri.getScheme() != null && !isValidScheme( uri ) ) {
			return false;
		}

		// Check authority requirement
		if ( !isValidAuthority( uri ) ) {
			return false;
		}

		// Check opaque URI restriction
		if ( !isValidOpaque( uri ) ) {
			return false;
		}

		return true;
	}

	private boolean isValidType(java.net.URI uri) {
		boolean isAbsolute = uri.isAbsolute(); // Has scheme

		switch ( type ) {
			case ABSOLUTE:
				return isAbsolute;
			case RELATIVE:
				return !isAbsolute;
			case ANY:
			default:
				return true;
		}
	}

	private boolean isValidScheme(java.net.URI uri) {
		if ( schemes == null || schemes.length == 0 ) {
			return true;
		}

		String uriScheme = uri.getScheme();
		if ( uriScheme == null ) {
			return true; // No scheme means relative URI, skip scheme check
		}

		// Case-insensitive scheme matching per RFC 3986
		String lowerScheme = uriScheme.toLowerCase( Locale.ROOT );
		for ( String allowedScheme : schemes ) {
			if ( allowedScheme.equals( lowerScheme ) ) {
				return true;
			}
		}
		return false;
	}

	private boolean isValidAuthority(java.net.URI uri) {
		String uriAuthority = uri.getAuthority();
		boolean hasAuthority = uriAuthority != null;

		switch ( authority ) {
			case REQUIRED:
				return hasAuthority;
			case FORBIDDEN:
				return !hasAuthority;
			case OPTIONAL:
			default:
				return true;
		}
	}

	private boolean isValidOpaque(java.net.URI uri) {
		if ( allowOpaque ) {
			return true;
		}

		// Opaque URIs have a scheme-specific part but no authority/path hierarchy
		// They are identified by: isAbsolute() && !isOpaque() means hierarchical absolute URI
		// isOpaque() returns true for opaque URIs like mailto:, urn:
		return !uri.isOpaque();
	}
}
