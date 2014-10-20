/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import java.io.InputStream;
import java.util.Set;
import javax.validation.Configuration;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Pattern.Flag;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.URLDef;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraintvalidators.RegexpURLValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.URLValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@code URL} constraint.
 *
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = { "HV-229", "HV-920" })
public class URLValidatorTest {

	private URLValidator urlValidator;
	private RegexpURLValidator regexpURLValidator;

	AnnotationDescriptor<URL> descriptor = new AnnotationDescriptor<URL>( URL.class );

	@BeforeMethod
	public void setUp() {
		descriptor = new AnnotationDescriptor<URL>( URL.class );
		urlValidator = new URLValidator();
		regexpURLValidator = new RegexpURLValidator();
	}

	@Test
	public void valid_urls_pass_validation() {
		URL url = AnnotationFactory.create( descriptor );
		urlValidator.initialize( url );
		assertValidUrls( urlValidator );

		regexpURLValidator.initialize( url );
		assertValidUrls( regexpURLValidator );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void url_validators_can_handle_character_sequences() {
		URL url = AnnotationFactory.create( descriptor );

		urlValidator.initialize( url );
		assertValidCharSequenceUrls( urlValidator );

		regexpURLValidator.initialize( url );
		assertValidCharSequenceUrls( regexpURLValidator );
	}

	@Test
	public void http_protocol_can_be_verified_explicitly() {
		descriptor.setValue( "protocol", "http" );
		URL url = AnnotationFactory.create( descriptor );

		urlValidator.initialize( url );
		assertHttpProtocolMatch( urlValidator );

		regexpURLValidator.initialize( url );
		assertHttpProtocolMatch( regexpURLValidator );
	}

	@Test
	public void file_protocol_can_be_verified_explicitly() {
		descriptor.setValue( "protocol", "file" );
		URL url = AnnotationFactory.create( descriptor );

		urlValidator.initialize( url );
		assertFileProtocolMatch( urlValidator );

		regexpURLValidator.initialize( url );
		assertFileProtocolMatch( regexpURLValidator );
	}

	@Test
	public void port_can_be_verified_explicitly() {
		descriptor.setValue( "port", 21 );
		URL url = AnnotationFactory.create( descriptor );

		urlValidator.initialize( url );
		assertPortMatch( urlValidator );

		regexpURLValidator.initialize( url );
		assertPortMatch( regexpURLValidator );
	}

	@Test
	public void host_can_be_verified_explicitly() {
		descriptor.setValue( "host", "foobar.com" );
		URL url = AnnotationFactory.create( descriptor );

		urlValidator.initialize( url );
		assertHostMatch( urlValidator );

		regexpURLValidator.initialize( url );
		assertHostMatch( regexpURLValidator );
	}

	@Test
	public void protocol_host_and_port_can_be_verified_explicitly() {
		descriptor.setValue( "protocol", "http" );
		descriptor.setValue( "host", "www.hibernate.org" );
		descriptor.setValue( "port", 80 );
		URL url = AnnotationFactory.create( descriptor );

		URLValidator validator = new URLValidator();
		validator.initialize( url );

		urlValidator.initialize( url );
		assertProtocolHostAnsPortMatch( urlValidator );

		regexpURLValidator.initialize( url );
		assertProtocolHostAnsPortMatch( regexpURLValidator );
	}

	@Test
	@TestForIssue(jiraKey = "HV-323")
	public void the_empty_string_is_considered_a_valid_url() {
		URL url = AnnotationFactory.create( descriptor );
		urlValidator.initialize( url );
		assertTrue( urlValidator.isValid( "", null ) );

		regexpURLValidator.initialize( url );
		assertTrue( regexpURLValidator.isValid( "", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-406")
	public void url_matching_can_be_refined_with_additional_regular_expression() {
		Validator validator = ValidatorUtil.getValidator();
		URLContainer container = new URLContainerAnnotated();
		runUrlContainerValidation( validator, container, true );
	}

	@Test
	@TestForIssue(jiraKey = "HV-406")
	public void explicit_regular_expression_can_be_specified_via_programmatic_configuration() {
		// now the same test with programmatic configuration
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( URLContainer.class )
				.property( "url", METHOD )
				.constraint( new URLDef().regexp( "^http://\\S+[\\.htm|\\.html]{1}$" ) );
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		URLContainer container = new URLContainerNoAnnotations();
		runUrlContainerValidation( validator, container, true );
	}

	@Test
	@TestForIssue(jiraKey = "HV-406")
	public void optional_regular_expression_can_be_refined_with_flags() {
		Validator validator = ValidatorUtil.getValidator();
		URLContainer container = new CaseInsensitiveURLContainerAnnotated();
		runUrlContainerValidation( validator, container, false );
	}

	@Test
	@TestForIssue(jiraKey = "HV-406")
	public void optional_regular_expression_can_be_refined_with_flags_using_programmatic_api() {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( URLContainer.class )
				.property( "url", METHOD )
				.constraint(
						new URLDef().regexp( "^http://\\S+[\\.htm|\\.html]{1}$" ).flags( Flag.CASE_INSENSITIVE )
				);
		config.addMapping( mapping );
		Validator validator = config.buildValidatorFactory().getValidator();

		URLContainer container = new URLContainerNoAnnotations();
		runUrlContainerValidation( validator, container, false );
	}

	@Test
	@TestForIssue(jiraKey = "HV-920")
	public void url_validator_using_regexp_only_can_be_configured_via_constraint_definition_contributor() {
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );

		config.addConstraintDefinitionContributor(
				new ConstraintDefinitionContributor() {
					@Override
					public void collectConstraintDefinitions(ConstraintDefinitionBuilder builder) {
						builder.constraint( URL.class )
								.includeExistingValidators( false )
								.validatedBy( RegexpURLValidator.class );
					}
				}
		);

		DelegatingConstraintValidatorFactory constraintValidatorFactory = new DelegatingConstraintValidatorFactory(
				config.getDefaultConstraintValidatorFactory()
		);
		config.constraintValidatorFactory( constraintValidatorFactory );

		assertDefaultURLConstraintValidatorOverridden( config, constraintValidatorFactory );
	}

	@Test
	@TestForIssue(jiraKey = "HV-920")
	public void url_validator_using_regexp_only_can_be_configured_via_xml() {
		Configuration config = ValidatorUtil.getConfiguration();

		InputStream mappingStream = URLValidatorTest.class.getResourceAsStream( "mapping.xml" );
		config.addMapping( mappingStream );

		DelegatingConstraintValidatorFactory constraintValidatorFactory = new DelegatingConstraintValidatorFactory(
				config.getDefaultConstraintValidatorFactory()
		);
		config.constraintValidatorFactory( constraintValidatorFactory );

		assertDefaultURLConstraintValidatorOverridden( config, constraintValidatorFactory );
	}

	private void assertDefaultURLConstraintValidatorOverridden(Configuration config,
			DelegatingConstraintValidatorFactory constraintValidatorFactory) {
		Validator validator = config.buildValidatorFactory().getValidator();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo() );

		assertNumberOfViolations( constraintViolations, 0 );
		assertEquals(
				constraintValidatorFactory.requestedConstraintValidators.size(),
				2, // @URL is a composing constraint, a @Pattern validator impl will also be requested
				"Wrong number of requested validator instances"
		);
		assertTrue(
				constraintValidatorFactory.requestedConstraintValidators.contains( RegexpURLValidator.class ),
				"The wrong validator type has been requested."
		);
	}

	private void runUrlContainerValidation(Validator validator, URLContainer container, boolean caseSensitive) {
		container.setUrl( "http://my.domain.com/index.html" );
		Set<ConstraintViolation<URLContainer>> violations = validator.validate( container );
		assertNumberOfViolations( violations, 0 );

		container.setUrl( "http://my.domain.com/index.htm" );
		violations = validator.validate( container );
		assertNumberOfViolations( violations, 0 );

		container.setUrl( "http://my.domain.com/index" );
		violations = validator.validate( container );
		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "must be a valid URL" );

		container.setUrl( "http://my.domain.com/index.asp" );
		violations = validator.validate( container );
		assertNumberOfViolations( violations, 1 );
		assertCorrectConstraintViolationMessages( violations, "must be a valid URL" );

		container.setUrl( "http://my.domain.com/index.HTML" );
		violations = validator.validate( container );
		assertNumberOfViolations( violations, caseSensitive ? 1 : 0 );
		if ( caseSensitive ) {
			assertCorrectConstraintViolationMessages( violations, "must be a valid URL" );
		}

	}

	private void assertValidUrls(ConstraintValidator<URL, CharSequence> validator) {
		assertTrue( validator.isValid( null, null ) );
		assertFalse( validator.isValid( "http", null ) );
		assertFalse( validator.isValid( "ftp//abc.de", null ) );
		assertTrue( validator.isValid( "ftp://abc.de", null ) );
	}

	private void assertValidCharSequenceUrls(ConstraintValidator<URL, CharSequence> validator) {
		assertFalse( validator.isValid( new MyCustomStringImpl( "ftp//abc.de" ), null ) );
		assertTrue( validator.isValid( new MyCustomStringImpl( "ftp://abc.de" ), null ) );
	}

	private void assertHttpProtocolMatch(ConstraintValidator<URL, CharSequence> validator) {
		assertFalse( validator.isValid( "ftp://abc.de", null ), "ftp urls should fail using " + validator );
		assertTrue( validator.isValid( "http://abc.de", null ), "http urls should pass using " + validator );
	}

	private void assertFileProtocolMatch(ConstraintValidator<URL, CharSequence> validator) {
		assertFalse( validator.isValid( "http://abc.de", null ), "http urls should fail using " + validator );
		assertTrue( validator.isValid( "file://Users/foobar/tmp", null ), "file urls should pass using " + validator );
	}

	private void assertPortMatch(ConstraintValidator<URL, CharSequence> validator) {
		assertFalse( validator.isValid( "ftp://abc.de", null ), "wrong/no port url should fail using " + validator );
		assertFalse(
				validator.isValid( "ftp://abc.de:1001", null ), "wrong/no port url should fail using " + validator
		);
		assertTrue( validator.isValid( "ftp://abc.de:21", null ), "correct port should match using " + validator );
	}

	private void assertHostMatch(ConstraintValidator<URL, CharSequence> validator) {
		assertFalse(
				validator.isValid( "http://fubar.com/this/is/foobar.html", null ),
				"wrong hostname should fail using " + validator
		);
		assertTrue(
				validator.isValid( "http://foobar.com/this/is/foobar.html", null ),
				"right host name should pass validation using " + validator
		);
	}

	private void assertProtocolHostAnsPortMatch(ConstraintValidator<URL, CharSequence> validator) {
		assertFalse(
				validator.isValid( "ftp://www.hibernate.org:80", null ),
				"wrong host/port should fail using " + validator
		);
		assertFalse(
				validator.isValid( "http://www.hibernate.com:80", null ),
				"wrong host/port should fail using " + validator
		);
		assertFalse(
				validator.isValid( "http://www.hibernate.org:81", null ),
				"wrong host/port should fail using " + validator
		);
		assertTrue(
				validator.isValid( "http://www.hibernate.org:80", null ),
				"correct host and port should match using " + validator
		);
	}

	private class Foo {
		@URL
		public String getUrl() {
			return "http://hibernate.org";
		}
	}


	private abstract static class URLContainer {
		public String url;

		public void setUrl(String url) {
			this.url = url;
		}

		@SuppressWarnings("unused")
		public String getUrl() {
			return url;
		}
	}

	private static class URLContainerAnnotated extends URLContainer {
		@Override
		@URL(regexp = "^http://\\S+[\\.htm|\\.html]{1}$")
		public String getUrl() {
			return url;
		}
	}

	private static class CaseInsensitiveURLContainerAnnotated extends URLContainer {

		@Override
		@URL(regexp = "^http://\\S+[\\.htm|\\.html]{1}$", flags = Flag.CASE_INSENSITIVE)
		public String getUrl() {
			return url;
		}
	}

	private static class URLContainerNoAnnotations extends URLContainer {
	}

	private static class DelegatingConstraintValidatorFactory implements ConstraintValidatorFactory {
		private final Set<Class<?>> requestedConstraintValidators = newHashSet();
		private final ConstraintValidatorFactory delegate;

		private DelegatingConstraintValidatorFactory(ConstraintValidatorFactory delegate) {
			this.delegate = delegate;
		}

		@Override
		public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
			requestedConstraintValidators.add( key );
			return delegate.getInstance( key );
		}

		@Override
		public void releaseInstance(ConstraintValidator<?, ?> instance) {
			delegate.releaseInstance( instance );
		}

		public Set<Class<?>> requestedConstraintValidators() {
			return requestedConstraintValidators;
		}
	}
}
