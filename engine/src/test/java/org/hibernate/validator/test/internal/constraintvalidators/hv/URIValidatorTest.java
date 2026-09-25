/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Pattern.Flag;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.URIDef;
import org.hibernate.validator.constraints.URI;
import org.hibernate.validator.internal.constraintvalidators.hv.URIValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for {@link URIValidator}.
 *
 * @author Andrea Boriero
 */
@TestForIssue(jiraKey = "HV-2254")
public class URIValidatorTest {

	private URIValidator validator;
	private ConstraintAnnotationDescriptor.Builder<URI> descriptorBuilder;
	private URI uriAnnotation;

	@BeforeMethod
	public void setUp() {
		validator = new URIValidator();
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( URI.class );
	}

	@Test
	public void testNullIsValid() {
		uriAnnotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( uriAnnotation );
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void testEmptyStringIsValid() {
		uriAnnotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( uriAnnotation );
		assertTrue( validator.isValid( "", null ) );
	}

	@Test
	public void testValidAbsoluteURIs() {
		uriAnnotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( uriAnnotation );
		assertTrue( validator.isValid( "http://example.com", null ) );
		assertTrue( validator.isValid( "https://example.com/path", null ) );
		assertTrue( validator.isValid( "ftp://ftp.example.com", null ) );
		assertTrue( validator.isValid( "file:///path/to/file", null ) );
		assertTrue( validator.isValid( "HTTP://EXAMPLE.COM", null ) ); // Case insensitive scheme
	}

	@Test
	public void testValidOpaqueURIs() {
		uriAnnotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( uriAnnotation );
		assertTrue( validator.isValid( "mailto:user@example.com", null ) );
		assertTrue( validator.isValid( "urn:isbn:0451450523", null ) );
		assertTrue( validator.isValid( "data:text/plain;base64,SGVsbG8=", null ) );
		assertTrue( validator.isValid( "tel:+1-816-555-1212", null ) );
	}

	@Test
	public void testValidRelativeURIs() {
		uriAnnotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( uriAnnotation );
		assertTrue( validator.isValid( "../path", null ) );
		assertTrue( validator.isValid( "/absolute/path", null ) );
		assertTrue( validator.isValid( "relative/path", null ) );
		assertTrue( validator.isValid( "./current", null ) );
	}

	@Test
	public void testValidIPv6URIs() {
		uriAnnotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( uriAnnotation );
		assertTrue( validator.isValid( "http://[2001:db8::1]/", null ) );
		assertTrue( validator.isValid( "http://[::1]/", null ) );
		assertTrue( validator.isValid( "http://[fe80::1%eth0]/", null ) ); // With zone ID
	}

	@Test
	public void testInvalidURIs() {
		uriAnnotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( uriAnnotation );
		assertFalse( validator.isValid( "ht tp://example.com", null ) ); // Space in scheme
		assertFalse( validator.isValid( "http://exa mple.com", null ) ); // Space in host
		assertFalse( validator.isValid( "123:path", null ) ); // Invalid scheme (starts with digit)
	}

	@Test
	public void testTypeAbsolute() {
		descriptorBuilder.setAttribute( "type", URI.Type.ABSOLUTE );
		uriAnnotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( uriAnnotation );

		assertTrue( validator.isValid( "http://example.com", null ) );
		assertTrue( validator.isValid( "mailto:user@example.com", null ) );
		assertFalse( validator.isValid( "../path", null ) );
		assertFalse( validator.isValid( "/absolute/path", null ) );
	}

	@Test
	public void testTypeRelative() {
		descriptorBuilder.setAttribute( "type", URI.Type.RELATIVE );
		uriAnnotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( uriAnnotation );

		assertTrue( validator.isValid( "../path", null ) );
		assertTrue( validator.isValid( "/absolute/path", null ) );
		assertFalse( validator.isValid( "http://example.com", null ) );
		assertFalse( validator.isValid( "mailto:user@example.com", null ) );
	}

	@Test
	public void testSchemeRestriction() {
		descriptorBuilder.setAttribute( "schemes", new String[] { "http", "https" } );
		uriAnnotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( uriAnnotation );

		assertTrue( validator.isValid( "http://example.com", null ) );
		assertTrue( validator.isValid( "https://example.com", null ) );
		assertTrue( validator.isValid( "HTTP://EXAMPLE.COM", null ) ); // Case insensitive
		assertTrue( validator.isValid( "HTTPS://EXAMPLE.COM", null ) );
		assertFalse( validator.isValid( "ftp://ftp.example.com", null ) );
		assertFalse( validator.isValid( "mailto:user@example.com", null ) );
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testInvalidConfigurationRelativeWithAllowOpaqueFalse() {
		descriptorBuilder.setAttribute( "type", URI.Type.RELATIVE );
		descriptorBuilder.setAttribute( "allowOpaque", false );
		uriAnnotation = descriptorBuilder.build().getAnnotation();
		validator.initialize( uriAnnotation );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2254")
	public void uri_matching_can_be_refined_with_additional_regular_expression() {
		Validator validator = ValidatorUtil.getValidator();
		URIContainer container = new URIContainerAnnotated();
		runUriContainerValidation( validator, container, true );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2254")
	public void explicit_regular_expression_can_be_specified_via_programmatic_configuration() {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( URIContainer.class )
				.getter( "uri" )
				.constraint( new URIDef().regexp( "^http://\\S+\\.(htm|html)$" ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		URIContainer container = new URIContainerNoAnnotations();
		runUriContainerValidation( validator, container, true );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2254")
	public void optional_regular_expression_can_be_refined_with_flags() {
		Validator validator = ValidatorUtil.getValidator();
		URIContainer container = new CaseInsensitiveURIContainerAnnotated();
		runUriContainerValidation( validator, container, false );
	}

	@Test
	@TestForIssue(jiraKey = "HV-2254")
	public void optional_regular_expression_can_be_refined_with_flags_using_programmatic_api() {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( URIContainer.class )
				.getter( "uri" )
				.constraint(
						new URIDef().regexp( "^http://\\S+\\.(htm|html)$" ).flags( Flag.CASE_INSENSITIVE )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		URIContainer container = new URIContainerNoAnnotations();
		runUriContainerValidation( validator, container, false );
	}

	private void runUriContainerValidation(Validator validator, URIContainer container, boolean caseSensitive) {
		container.setUri( "http://my.domain.com/index.html" );
		Set<ConstraintViolation<URIContainer>> violations = validator.validate( container );
		assertNoViolations( violations );

		container.setUri( "http://my.domain.com/index.htm" );
		violations = validator.validate( container );
		assertNoViolations( violations );

		container.setUri( "http://my.domain.com/index" );
		violations = validator.validate( container );
		assertThat( violations ).containsOnlyViolations(
				violationOf( URI.class ).withMessage( "must be a valid URI" )
		);

		container.setUri( "http://my.domain.com/index.asp" );
		violations = validator.validate( container );
		assertThat( violations ).containsOnlyViolations(
				violationOf( URI.class ).withMessage( "must be a valid URI" )
		);

		container.setUri( "http://my.domain.com/index.HTML" );
		violations = validator.validate( container );
		if ( caseSensitive ) {
			assertThat( violations ).containsOnlyViolations(
					violationOf( URI.class ).withMessage( "must be a valid URI" )
			);
		}
		else {
			assertNoViolations( violations );
		}
	}

	private abstract static class URIContainer {
		public String uri;

		public void setUri(String uri) {
			this.uri = uri;
		}

		@SuppressWarnings("unused")
		public String getUri() {
			return uri;
		}
	}

	private static class URIContainerAnnotated extends URIContainer {
		@Override
		@URI(regexp = "^http://\\S+\\.(htm|html)$")
		public String getUri() {
			return uri;
		}
	}

	private static class CaseInsensitiveURIContainerAnnotated extends URIContainer {

		@Override
		@URI(regexp = "^http://\\S+\\.(htm|html)$", flags = Flag.CASE_INSENSITIVE)
		public String getUri() {
			return uri;
		}
	}

	private static class URIContainerNoAnnotations extends URIContainer {
	}
}
