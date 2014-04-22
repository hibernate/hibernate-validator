/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011-2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.SafeHtml.WhiteListType;
import org.hibernate.validator.internal.constraintvalidators.SafeHtmlValidator;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotationfactory.AnnotationFactory;
import org.hibernate.validator.testutil.TestForIssue;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNumberOfViolations;
import static org.hibernate.validator.testutil.ValidatorUtil.getValidator;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Unit test for {@link SafeHtmlValidator}.
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 */
public class SafeHtmlValidatorTest {

	private AnnotationDescriptor<SafeHtml> descriptor;

	@BeforeMethod
	public void setUp() {
		descriptor = new AnnotationDescriptor<SafeHtml>( SafeHtml.class );
	}

	@Test
	public void testNullValue() throws Exception {
		descriptor.setValue( "whitelistType", WhiteListType.BASIC );

		assertTrue( getSafeHtmlValidator().isValid( null, null ) );
	}

	@Test
	public void testInvalidScriptTagIncluded() throws Exception {
		descriptor.setValue( "whitelistType", WhiteListType.BASIC );

		assertFalse( getSafeHtmlValidator().isValid( "Hello<script>alert('Doh')</script>World !", null ) );
	}

	@Test
	public void testValid() throws Exception {
		descriptor.setValue( "whitelistType", WhiteListType.BASIC );

		assertTrue( getSafeHtmlValidator().isValid( "<p><a href='http://example.com/'>Link</a></p>", null ) );
	}

	@Test
	public void testAdditionalTags() throws Exception {
		descriptor.setValue( "additionalTags", new String[] { "script" } );

		assertTrue( getSafeHtmlValidator().isValid( "Hello<script>alert('Doh')</script>World !", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-817")
	public void testDivNotAllowedInBasicWhiteList() throws Exception {
		descriptor.setValue( "whitelistType", WhiteListType.BASIC );

		SafeHtmlValidator validator = getSafeHtmlValidator();
		assertFalse( validator.isValid( "<div>test</div>", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-817")
	public void testDivAllowedInRelaxedWhiteList() throws Exception {
		descriptor.setValue( "whitelistType", WhiteListType.RELAXED );

		assertTrue( getSafeHtmlValidator().isValid( "<div>test</div>", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-817")
	public void testDivWithWhiteListedClassAttribute() throws Exception {
		descriptor.setValue( "whitelistType", WhiteListType.RELAXED );

		AnnotationDescriptor<SafeHtml.Tag> tagDescriptor = new AnnotationDescriptor<SafeHtml.Tag>( SafeHtml.Tag.class );
		tagDescriptor.setValue( "name", "div" );
		tagDescriptor.setValue( "attributes", new String[] { "class" } );
		SafeHtml.Tag tag = AnnotationFactory.create( tagDescriptor );
		descriptor.setValue( "additionalTagsWithAttributes", new SafeHtml.Tag[] { tag } );

		assertTrue(
				getSafeHtmlValidator().isValid( "<div class='foo'>test</div>", null ),
				"class attribute should be white listed"
		);
		assertFalse(
				getSafeHtmlValidator().isValid( "<div style='foo'>test</div>", null ),
				"style attribute is not white listed"
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-817")
	public void testDivWithWhiteListedStyleAttribute() throws Exception {
		Validator validator = getValidator();
		Set<ConstraintViolation<Foo>> constraintViolations = validator.validate( new Foo( "<div style='foo'>test</div>" ) );
		assertNumberOfViolations( constraintViolations, 0 );

		// the attributes are optional - allowing <div class> also allows just <div>
		constraintViolations = validator.validate( new Foo( "<div>test</div>" ) );
		assertNumberOfViolations( constraintViolations, 0 );

		constraintViolations = validator.validate( new Foo( "<div class='foo'>test</div>" ) );
		assertNumberOfViolations( constraintViolations, 1 );
	}

	@Test
	@TestForIssue(jiraKey = "HV-873")
	public void testValidationOfInvalidFragment() throws Exception {
		descriptor.setValue( "whitelistType", WhiteListType.NONE );

		assertFalse( getSafeHtmlValidator().isValid( "<td>1234qwer</td>", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-873")
	public void testValidationOfValidFragment() throws Exception {
		descriptor.setValue( "whitelistType", WhiteListType.RELAXED );

		assertTrue( getSafeHtmlValidator().isValid( "<td>1234qwer</td>", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-873")
	public void testValidationOfTextFragment() throws Exception {
		descriptor.setValue( "whitelistType", WhiteListType.NONE );

		assertTrue( getSafeHtmlValidator().isValid( "Foobar", null ) );
	}

	private SafeHtmlValidator getSafeHtmlValidator() {
		SafeHtml p = AnnotationFactory.create( descriptor );
		SafeHtmlValidator validator = new SafeHtmlValidator();
		validator.initialize( p );
		return validator;
	}

	public static class Foo {
		@SafeHtml(
				whitelistType = WhiteListType.BASIC,
				additionalTagsWithAttributes = @SafeHtml.Tag(name = "div", attributes = { "style" })
		)
		String source;

		public Foo(String source) {
			this.source = source;
		}
	}
}
