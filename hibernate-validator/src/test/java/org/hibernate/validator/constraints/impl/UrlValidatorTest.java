// $Id: LengthValidatorTest.java 17521 2009-09-16 12:50:41Z hardy.ferentschik $
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
package org.hibernate.validator.constraints.impl;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.util.annotationfactory.AnnotationFactory;

/**
 * Tests the {@code Url} constraint.
 *
 * @author Hardy Ferentschik
 */
public class URLValidatorTest {

	@Test
	public void testIsValidUrl() {
		AnnotationDescriptor<URL> descriptor = new AnnotationDescriptor<URL>( URL.class );
		descriptor.setValue( "message", "{org.hibernate.validator.constraints.URL.message}" );
		URL url = AnnotationFactory.create( descriptor );
		URLValidator validator = new URLValidator();
		validator.initialize( url );
		assertTrue( validator.isValid( null, null ) );
		assertFalse( validator.isValid( "", null ) );
		assertFalse( validator.isValid( "http", null ) );
		assertFalse( validator.isValid( "ftp//abc.de", null ) );
		assertTrue( validator.isValid( "ftp://abc.de", null ) );
	}
}