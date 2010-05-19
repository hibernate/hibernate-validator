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
package org.hibernate.validator.test.constraints.impl;

import org.testng.annotations.Test;

import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.impl.URLValidator;
import org.hibernate.validator.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.util.annotationfactory.AnnotationFactory;

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
	public void testIsValidEmptyString() {
		// HV-323
		AnnotationDescriptor<URL> descriptor = new AnnotationDescriptor<URL>( URL.class );
		descriptor.setValue( "protocol", "http" );
		descriptor.setValue( "host", "www.hibernate.org" );
		descriptor.setValue( "port", 80 );
		URL url = AnnotationFactory.create( descriptor );
		URLValidator validator = new URLValidator();
		validator.initialize( url );

		assertTrue( validator.isValid( "", null ) );
	}
}