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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.WebSafe;
import org.hibernate.validator.constraints.WebSafe.WhiteListType;
import org.hibernate.validator.constraints.impl.WebSafeValidator;
import org.hibernate.validator.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.util.annotationfactory.AnnotationFactory;
import org.jsoup.safety.Whitelist;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Unit test for {@link WebSafeValidator}.
 * 
 * @author George Gastaldi
 */
public class WebSafeValidatorTest {

	private static WebSafeValidator validator;

	@BeforeClass
	public static void init() {
		AnnotationDescriptor<WebSafe> descriptor = new AnnotationDescriptor<WebSafe>( WebSafe.class );
		descriptor.setValue( "value", WhiteListType.BASIC);
		WebSafe p = AnnotationFactory.create( descriptor );

		validator = new WebSafeValidator();
		validator.initialize( p );
	}

	@Test
	public void testNullValue() throws Exception {
		assertTrue( validator.isValid( null, null ) );
	}

	@Test
	public void testInvalidScriptTagIncluded() throws Exception {
		assertFalse( validator.isValid( "Hello<script>alert('Doh')</script>World !", null ) );
	}

	@Test
	public void testValid() throws Exception {
		assertTrue( validator.isValid( "<p><a href='http://example.com/'>Link</a></p>", null ) );
	}

}
