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
package org.hibernate.validator.test.internal.constraintvalidators;

import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.constraints.Pattern.Flag;

import org.testng.annotations.Test;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.defs.URLDef;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.internal.constraintvalidators.URLValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.TestForIssue;
import org.hibernate.validator.testutil.ValidatorUtil;

import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertCorrectConstraintViolationMessages;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests the {@code URL} constraint. See HV-229
 *
 * @author Hardy Ferentschik
 */
public class URLValidatorTest {

	@Test
	public void testIsValidUrl() {
		AnnotationDescriptor<URL> descriptor = new AnnotationDescriptor<URL>( URL.class );
		URL url = AnnotationFactory.create( descriptor );
		URLValidator validator = new URLValidator();
		validator.initialize( url );

		assertTrue( validator.isValid( null, null ) );
		assertFalse( validator.isValid( "http", null ) );
		assertFalse( validator.isValid( "ftp//abc.de", null ) );
		assertTrue( validator.isValid( "ftp://abc.de", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-502")
	public void testIsValidUrlWithCharSequence() {
		AnnotationDescriptor<URL> descriptor = new AnnotationDescriptor<URL>( URL.class );
		URL url = AnnotationFactory.create( descriptor );
		URLValidator validator = new URLValidator();
		validator.initialize( url );

		assertFalse( validator.isValid( new MyCustomStringImpl( "ftp//abc.de" ), null ) );
		assertTrue( validator.isValid( new MyCustomStringImpl( "ftp://abc.de" ), null ) );
	}

	@Test
	public void testIsValidUrlWithProtocolSpecified() {
		AnnotationDescriptor<URL> descriptor = new AnnotationDescriptor<URL>( URL.class );
		descriptor.setValue( "protocol", "http" );
		URL url = AnnotationFactory.create( descriptor );
		URLValidator validator = new URLValidator();
		validator.initialize( url );

		assertFalse( validator.isValid( "ftp://abc.de", null ) );
		assertTrue( validator.isValid( "http://abc.de", null ) );

		descriptor = new AnnotationDescriptor<URL>( URL.class );
		descriptor.setValue( "protocol", "file" );
		url = AnnotationFactory.create( descriptor );
		validator = new URLValidator();
		validator.initialize( url );
		assertFalse( validator.isValid( "http://abc.de", null ) );
		assertTrue( validator.isValid( "file://Users/foobar/tmp", null ) );
	}

	@Test
	public void testIsValidUrlWithPortSpecified() {
		AnnotationDescriptor<URL> descriptor = new AnnotationDescriptor<URL>( URL.class );
		descriptor.setValue( "port", 21 );
		URL url = AnnotationFactory.create( descriptor );
		URLValidator validator = new URLValidator();
		validator.initialize( url );

		assertFalse( validator.isValid( "ftp://abc.de", null ) );
		assertTrue( validator.isValid( "ftp://abc.de:21", null ) );
	}

	@Test
	public void testIsValidUrlWithHostSpecified() {
		AnnotationDescriptor<URL> descriptor = new AnnotationDescriptor<URL>( URL.class );
		descriptor.setValue( "host", "foobar.com" );
		URL url = AnnotationFactory.create( descriptor );
		URLValidator validator = new URLValidator();
		validator.initialize( url );

		assertFalse( validator.isValid( "http://fubar.com/this/is/foobar.html", null ) );
		assertTrue( validator.isValid( "http://foobar.com/this/is/foobar.html", null ) );
	}

	@Test
	public void testIsValidUrlWithProtocolHostAndPort() {
		AnnotationDescriptor<URL> descriptor = new AnnotationDescriptor<URL>( URL.class );
		descriptor.setValue( "protocol", "http" );
		descriptor.setValue( "host", "www.hibernate.org" );
		descriptor.setValue( "port", 80 );
		URL url = AnnotationFactory.create( descriptor );
		URLValidator validator = new URLValidator();
		validator.initialize( url );

		assertFalse( validator.isValid( "ftp://www#hibernate#org:80", null ) );
		assertTrue( validator.isValid( "http://www.hibernate.org:80", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-323")
	public void testIsValidEmptyString() {
		AnnotationDescriptor<URL> descriptor = new AnnotationDescriptor<URL>( URL.class );
		descriptor.setValue( "protocol", "http" );
		descriptor.setValue( "host", "www.hibernate.org" );
		descriptor.setValue( "port", 80 );
		URL url = AnnotationFactory.create( descriptor );
		URLValidator validator = new URLValidator();
		validator.initialize( url );

		assertTrue( validator.isValid( "", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-406")
	public void testRegExp() {
		// first run the test with @URL configured via annotations
		Validator validator = ValidatorUtil.getValidator();
		URLContainer container = new URLContainerAnnotated();
		runUrlContainerValidation( validator, container, true );

		// now the same test with programmatic configuration
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( URLContainer.class )
				.property( "url", METHOD )
				.constraint( new URLDef().regexp( "^http://\\S+[\\.htm|\\.html]{1}$" ) );
		config.addMapping( mapping );
		validator = config.buildValidatorFactory().getValidator();

		container = new URLContainerNoAnnotations();
		runUrlContainerValidation( validator, container, true );
	}

	@Test
	@TestForIssue(jiraKey = "HV-406")
	public void testRegExpCaseInsensitive() {

		// first run the test with @URL configured via annotations
		Validator validator = ValidatorUtil.getValidator();
		URLContainer container = new CaseInsensitiveURLContainerAnnotated();
		runUrlContainerValidation( validator, container, false );

		// now the same test with programmatic configuration
		HibernateValidatorConfiguration config = ValidatorUtil.getConfiguration( HibernateValidator.class );
		ConstraintMapping mapping = config.createConstraintMapping();
		mapping.type( URLContainer.class )
				.property( "url", METHOD )
				.constraint(
						new URLDef().regexp( "^http://\\S+[\\.htm|\\.html]{1}$" ).flags( Flag.CASE_INSENSITIVE )
				);
		config.addMapping( mapping );
		validator = config.buildValidatorFactory().getValidator();

		container = new URLContainerNoAnnotations();
		runUrlContainerValidation( validator, container, false );
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
}
