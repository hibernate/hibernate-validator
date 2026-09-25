/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.constraints;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.OverridesAttribute;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.constraints.URI.List;

/**
 * Validates that the annotated character sequence is a valid URI according to
 * <a href="https://www.ietf.org/rfc/rfc3986.txt">RFC 3986</a>.
 * <p>
 * Validation is performed using {@link java.net.URI} which validates the URI syntax.
 * Unlike {@link URL}, this constraint accepts all URI forms including:
 * <ul>
 * <li>Absolute URIs with schemes: {@code http://example.com}, {@code ftp://ftp.example.com}</li>
 * <li>Opaque URIs: {@code mailto:user@example.com}, {@code urn:isbn:0451450523}, {@code data:text/plain;base64,SGVsbG8=}</li>
 * <li>Relative URI references: {@code ../path}, {@code /absolute/path}, {@code relative/path}</li>
 * <li>IPv6 addresses: {@code http://[2001:db8::1]/}, {@code http://[fe80::1%eth0]/}</li>
 * </ul>
 * <p>
 * The constraint provides fine-grained control through various attributes:
 * <ul>
 * <li>{@link #type()} - enforce absolute or relative URIs</li>
 * <li>{@link #schemes()} - restrict allowed URI schemes</li>
 * <li>{@link #authority()} - require, forbid, or allow authority component (//host:port)</li>
 * <li>{@link #allowOpaque()} - control acceptance of opaque URIs</li>
 * <li>{@link #regexp()} and {@link #flags()} - apply additional regex validation</li>
 * </ul>
 * <p>
 * The supported type is {@code CharSequence}. {@code null} is considered valid.
 * Empty string is valid per RFC 3986 (valid relative URI reference).
 *
 * @author Andrea Boriero
 * @since 9.2
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Repeatable(List.class)
@ReportAsSingleViolation
@Pattern(regexp = ".*")
@Incubating
public @interface URI {

	String message() default "{org.hibernate.validator.constraints.URI.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return the required URI type (absolute, relative, or any)
	 * Per default any valid URI or relative reference is allowed
	 */
	Type type() default Type.ANY;

	/**
	 * @return the allowed URI schemes (e.g., {@code {"http", "https"}}). Empty array means all schemes are allowed.
	 * Scheme matching is case-insensitive per RFC 3986.
	 * Per default all schemes are allowed.
	 */
	String[] schemes() default { };

	/**
	 * @return the authority component requirement (presence of {@code //host:port})
	 * Per default authority is optional
	 */
	AuthorityRequirement authority() default AuthorityRequirement.OPTIONAL;

	/**
	 * @return {@code true} if opaque URIs are accepted, {@code false} to restrict to hierarchical URIs only
	 * <p>
	 * Opaque URIs (RFC 3986 §3) have a scheme-specific part but no hierarchical structure.
	 * They follow the syntax {@code scheme:scheme-specific-part} without authority or path components.
	 * <p>
	 * Examples of opaque URIs:
	 * <ul>
	 * <li>{@code mailto:user@example.com} - email addresses</li>
	 * <li>{@code urn:isbn:0451450523} - URN (Uniform Resource Name)</li>
	 * <li>{@code tel:+1-816-555-1212} - telephone numbers</li>
	 * <li>{@code data:text/plain;base64,SGVsbG8=} - inline data</li>
	 * </ul>
	 * <p>
	 * Hierarchical URIs follow the syntax {@code scheme://authority/path?query#fragment}
	 * and include URIs like {@code http://example.com/path}, {@code ftp://ftp.example.com},
	 * and {@code file:///path/to/file}.
	 * <p>
	 * When set to {@code false}, only hierarchical URIs are considered valid.
	 * Per default opaque URIs are allowed.
	 */
	boolean allowOpaque() default true;

	/**
	 * @return an additional regular expression the annotated URI must match. The default is any string ('.*')
	 */
	@OverridesAttribute(constraint = Pattern.class, name = "regexp")
	String regexp() default ".*";

	/**
	 * @return flags used in combination with {@link #regexp()} to specify regex options
	 */
	@OverridesAttribute(constraint = Pattern.class, name = "flags")
	Pattern.Flag[] flags() default { };

	/**
	 * URI type classification
	 */
	enum Type {
		/**
		 * Any valid URI or relative reference
		 */
		ANY,

		/**
		 * Must be an absolute URI with a scheme (RFC 3986 §4.3)
		 */
		ABSOLUTE,

		/**
		 * Must be a relative reference without a scheme (RFC 3986 §4.2)
		 */
		RELATIVE
	}

	/**
	 * Authority component requirement
	 */
	enum AuthorityRequirement {
		/**
		 * Authority can be present or absent
		 */
		OPTIONAL,

		/**
		 * Must have authority component ({@code //host})
		 */
		REQUIRED,

		/**
		 * Must NOT have authority component (e.g., URNs or relative paths)
		 */
		FORBIDDEN
	}

	/**
	 * Defines several {@code @URI} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		URI[] value();
	}
}
