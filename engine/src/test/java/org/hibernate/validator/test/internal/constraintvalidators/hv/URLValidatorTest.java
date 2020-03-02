/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.internal.util.CollectionHelper.newHashSet;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.InputStream;
import java.util.Set;

import jakarta.validation.Configuration;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Pattern.Flag;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.URLDef;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraintvalidators.RegexpURLValidator;
import org.hibernate.validator.internal.constraintvalidators.hv.URLValidator;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.MyCustomStringImpl;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutils.ValidatorUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the {@code URL} constraint.
 *
 * @author Hardy Ferentschik
 */
@TestForIssue(jiraKey = { "HV-229", "HV-920" })
public class URLValidatorTest {

	private URLValidator urlValidator;
	private RegexpURLValidator regexpURLValidator;

	private ConstraintAnnotationDescriptor.Builder<URL> descriptorBuilder;

	@BeforeMethod
	public void setUp() {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<URL>( URL.class );
		urlValidator = new URLValidator();
		regexpURLValidator = new RegexpURLValidator();
	}

	@Test
	public void valid_urls_pass_validation() {
		URL url = descriptorBuilder.build().getAnnotation();
		urlValidator.initialize( url );
		assertValidUrls( urlValidator );

		regexpURLValidator.initialize( url );
		assertValidUrls( regexpURLValidator );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void url_validators_can_handle_character_sequences() {
		URL url = descriptorBuilder.build().getAnnotation();

		urlValidator.initialize( url );
		assertValidCharSequenceUrls( urlValidator );

		regexpURLValidator.initialize( url );
		assertValidCharSequenceUrls( regexpURLValidator );
	}

	@Test
	public void http_protocol_can_be_verified_explicitly() {
		descriptorBuilder.setAttribute( "protocol", "http" );
		URL url = descriptorBuilder.build().getAnnotation();

		urlValidator.initialize( url );
		assertHttpProtocolMatch( urlValidator );

		regexpURLValidator.initialize( url );
		assertHttpProtocolMatch( regexpURLValidator );
	}

	@Test
	public void file_protocol_can_be_verified_explicitly() {
		descriptorBuilder.setAttribute( "protocol", "file" );
		URL url = descriptorBuilder.build().getAnnotation();

		urlValidator.initialize( url );
		assertFileProtocolMatch( urlValidator );

		regexpURLValidator.initialize( url );
		assertFileProtocolMatch( regexpURLValidator );
	}

	@Test
	public void port_can_be_verified_explicitly() {
		descriptorBuilder.setAttribute( "port", 21 );
		URL url = descriptorBuilder.build().getAnnotation();

		urlValidator.initialize( url );
		assertPortMatch( urlValidator );

		regexpURLValidator.initialize( url );
		assertPortMatch( regexpURLValidator );
	}

	@Test
	public void host_can_be_verified_explicitly() {
		descriptorBuilder.setAttribute( "host", "foobar.com" );
		URL url = descriptorBuilder.build().getAnnotation();

		urlValidator.initialize( url );
		assertHostMatch( urlValidator );

		regexpURLValidator.initialize( url );
		assertHostMatch( regexpURLValidator );
	}

	@Test
	public void protocol_host_and_port_can_be_verified_explicitly() {
		descriptorBuilder.setAttribute( "protocol", "http" );
		descriptorBuilder.setAttribute( "host", "www.hibernate.org" );
		descriptorBuilder.setAttribute( "port", 80 );
		URL url = descriptorBuilder.build().getAnnotation();

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
		URL url = descriptorBuilder.build().getAnnotation();
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
				.getter( "url" )
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
				.getter( "url" )
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

		ConstraintMapping constraintMapping = config.createConstraintMapping();

		constraintMapping
				.constraintDefinition( URL.class )
				.includeExistingValidators( false )
				.validatedBy( RegexpURLValidator.class );

		config.addMapping( constraintMapping );

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

		assertNoViolations( constraintViolations );
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
		assertNoViolations( violations );

		container.setUrl( "http://my.domain.com/index.htm" );
		violations = validator.validate( container );
		assertNoViolations( violations );

		container.setUrl( "http://my.domain.com/index" );
		violations = validator.validate( container );
		assertThat( violations ).containsOnlyViolations(
				violationOf( URL.class ).withMessage( "must be a valid URL" )
		);

		container.setUrl( "http://my.domain.com/index.asp" );
		violations = validator.validate( container );
		assertThat( violations ).containsOnlyViolations(
				violationOf( URL.class ).withMessage( "must be a valid URL" )
		);

		container.setUrl( "http://my.domain.com/index.HTML" );
		violations = validator.validate( container );
		if ( caseSensitive ) {
			assertThat( violations ).containsOnlyViolations(
					violationOf( URL.class ).withMessage( "must be a valid URL" )
			);
		}
		else {
			assertNoViolations( violations );
		}

	}

	private void assertValidUrls(ConstraintValidator<URL, CharSequence> validator) {
		//valid urls
		assertTrue( validator.isValid( null, null ) );
		assertTrue( validator.isValid( "ftp://abc.de", null ) );
		assertTrue( validator.isValid( "http://foo.com/blah_blah", null ) );
		assertTrue( validator.isValid( "http://foo.com/blah_blah/", null ) );
		assertTrue( validator.isValid( "http://foo.com/blah_blah_(wikipedia)", null ) );
		assertTrue( validator.isValid( "http://foo.com/blah_blah_(wikipedia)_(again)", null ) );
		assertTrue( validator.isValid( "http://www.example.com/wpstyle/?p=364", null ) );
		assertTrue( validator.isValid( "https://www.example.com/foo/?bar=baz&inga=42&quux", null ) );
		assertTrue( validator.isValid( "http://✪df.ws/123", null ) );
		assertTrue( validator.isValid( "http://userid:password@example.com:8080", null ) );
		assertTrue( validator.isValid( "http://userid:password@example.com:8080/", null ) );
		assertTrue( validator.isValid( "http://userid@example.com", null ) );
		assertTrue( validator.isValid( "http://userid@example.com/", null ) );
		assertTrue( validator.isValid( "http://userid@example.com:8080", null ) );
		assertTrue( validator.isValid( "http://userid@example.com:8080/", null ) );
		assertTrue( validator.isValid( "http://userid:password@example.com", null ) );
		assertTrue( validator.isValid( "http://userid:password@example.com/", null ) );
		assertTrue( validator.isValid( "http://142.42.1.1/", null ) );
		assertTrue( validator.isValid( "http://142.42.1.1:8080/", null ) );
		assertTrue( validator.isValid( "http://➡.ws/䨹", null ) );
		assertTrue( validator.isValid( "http://⌘.ws", null ) );
		assertTrue( validator.isValid( "http://⌘.ws/", null ) );
		assertTrue( validator.isValid( "http://foo.com/blah_(wikipedia)#cite-1", null ) );
		assertTrue( validator.isValid( "http://foo.com/blah_(wikipedia)_blah#cite-1", null ) );
		assertTrue( validator.isValid( "http://foo.com/unicode_(✪)_in_parens", null ) );
		assertTrue( validator.isValid( "http://foo.com/(something)?after=parens", null ) );
		assertTrue( validator.isValid( "http://☺.damowmow.com/", null ) );
		assertTrue( validator.isValid( "http://code.google.com/events/#&product=browser", null ) );
		assertTrue( validator.isValid( "http://j.mp", null ) );
		assertTrue( validator.isValid( "ftp://foo.bar/baz", null ) );
		assertTrue( validator.isValid( "http://foo.bar/?q=Test%20URL-encoded%20stuff", null ) );
		assertTrue( validator.isValid( "http://مثال.إختبار", null ) );
		assertTrue( validator.isValid( "http://例子.测试", null ) );
		assertTrue( validator.isValid( "http://उदाहरण.परीक्षा", null ) );
		assertTrue( validator.isValid( "http://-.~_!$&'()*+,;=:%40:80%2f::::::@example.com", null ) );
		assertTrue( validator.isValid( "http://1337.net", null ) );
		assertTrue( validator.isValid( "http://a.b-c.de", null ) );
		assertTrue( validator.isValid( "http://223.255.255.254", null ) );
		assertTrue( validator.isValid( "http://[2001:0db8:0a0b:12f0:0000:0000:0000:0001]", null ) );
		assertTrue( validator.isValid( "http://xn--80ahgue5b.xn--p-8sbkgc5ag7bhce.xn--ba-lmcq", null ) );
		assertTrue( validator.isValid( "http://xn--fken-gra.no", null ) );
		assertTrue( validator.isValid( "http://a.b--c.de/", null ) );

		// invalid urls:
		assertFalse( validator.isValid( "http", null ) );
		assertFalse( validator.isValid( "ftp//abc.de", null ) );
		assertFalse( validator.isValid( "//", null ) );
		assertFalse( validator.isValid( "//a", null ) );
		assertFalse( validator.isValid( "///", null ) );
		assertFalse( validator.isValid( "///a", null ) );
		assertFalse( validator.isValid( "foo.com", null ) );
		assertFalse( validator.isValid( ":// should fail", null ) );

		if ( validator instanceof URLValidator ) {
			// 'exotic' protocols are considered valid using RegexpURLValidator but not URLValidator
			// as the last one doesn't allow unknown protocols
			assertFalse( validator.isValid( "rdar://1234", null ) );
			assertFalse( validator.isValid( "ftps://foo.bar/", null ) );
			assertFalse( validator.isValid( "h://test", null ) );
		}

		if ( validator instanceof RegexpURLValidator ) {
			assertFalse( validator.isValid( "http://", null ) );
			assertFalse( validator.isValid( "http://.", null ) );
			assertFalse( validator.isValid( "http://..", null ) );
			assertFalse( validator.isValid( "http://../", null ) );
			assertFalse( validator.isValid( "http://?", null ) );
			assertFalse( validator.isValid( "http://??", null ) );
			assertFalse( validator.isValid( "http://??/", null ) );
			assertFalse( validator.isValid( "http://#", null ) );
			assertFalse( validator.isValid( "http://##", null ) );
			assertFalse( validator.isValid( "http://##/", null ) );
			assertFalse( validator.isValid( "http://foo.bar?q=Spaces should be encoded", null ) );
			assertFalse( validator.isValid( "http:///a", null ) );
			assertFalse( validator.isValid( "http:// shouldfail.com", null ) );
			assertFalse( validator.isValid( "http://foo.bar/foo(bar)baz quux", null ) );
			assertFalse( validator.isValid( "http://-error-.invalid/", null ) );
			assertFalse( validator.isValid( "http://-a.b.co", null ) );
			assertFalse( validator.isValid( "http://a.b-.co", null ) );
//			assertFalse( validator.isValid( "http://123.123.123", null ) );
//			assertFalse( validator.isValid( "http://3628126748", null ) );
			assertFalse( validator.isValid( "http://.www.foo.bar/", null ) );
			assertFalse( validator.isValid( "http://www.foo.bar./", null ) );
			assertFalse( validator.isValid( "http://.www.foo.bar./", null ) );
			assertFalse( validator.isValid( "http://2001:0db8:0a0b:12f0:0000:0000:0000:0001", null ) );
		}
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
