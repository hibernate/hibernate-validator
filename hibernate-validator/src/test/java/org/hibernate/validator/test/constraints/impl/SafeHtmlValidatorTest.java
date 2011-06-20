/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.SafeHtml.WhiteListType;
import org.hibernate.validator.constraints.impl.SafeHtmlValidator;
import org.hibernate.validator.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.util.annotationfactory.AnnotationFactory;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link SafeHtmlValidator}.
 *
 * @author George Gastaldi
 */
public class SafeHtmlValidatorTest {

	@Test
	public void testNullValue() throws Exception {
		AnnotationDescriptor<SafeHtml> descriptor = new AnnotationDescriptor<SafeHtml>( SafeHtml.class );
		descriptor.setValue( "whitelistType", WhiteListType.BASIC );
		SafeHtml p = AnnotationFactory.create( descriptor );

		SafeHtmlValidator validator = new SafeHtmlValidator();
		validator.initialize( p );
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void testInvalidScriptTagIncluded() throws Exception {
		AnnotationDescriptor<SafeHtml> descriptor = new AnnotationDescriptor<SafeHtml>( SafeHtml.class );
		descriptor.setValue( "whitelistType", WhiteListType.BASIC );
		SafeHtml p = AnnotationFactory.create( descriptor );

		SafeHtmlValidator validator = new SafeHtmlValidator();
		validator.initialize( p );
		assertFalse( validator.isValid( "Hello<script>alert('Doh')</script>World !", null ) );
	}

	@Test
	public void testValid() throws Exception {
		AnnotationDescriptor<SafeHtml> descriptor = new AnnotationDescriptor<SafeHtml>( SafeHtml.class );
		descriptor.setValue( "whitelistType", WhiteListType.BASIC );
		SafeHtml p = AnnotationFactory.create( descriptor );

		SafeHtmlValidator validator = new SafeHtmlValidator();
		validator.initialize( p );
		assertTrue( validator.isValid( "<p><a href='http://example.com/'>Link</a></p>", null ) );
	}

	@Test
	public void testAdditionalTags() throws Exception {
		AnnotationDescriptor<SafeHtml> descriptor = new AnnotationDescriptor<SafeHtml>( SafeHtml.class );
		descriptor.setValue( "additionalTags", new String[] { "script" } );
		SafeHtml p = AnnotationFactory.create( descriptor );

		SafeHtmlValidator validator = new SafeHtmlValidator();
		validator.initialize( p );
		assertTrue( validator.isValid( "Hello<script>alert('Doh')</script>World !", null ) );
	}
}
