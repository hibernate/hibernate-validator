/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.constraintvalidators.hv;

import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertNoViolations;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.assertThat;
import static org.hibernate.validator.testutil.ConstraintViolationAssert.violationOf;
import static org.hibernate.validator.testutils.ValidatorUtil.getValidator;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.SafeHtml.WhiteListType;
import org.hibernate.validator.internal.constraintvalidators.hv.SafeHtmlValidator;
import org.hibernate.validator.internal.util.annotation.AnnotationDescriptor;
import org.hibernate.validator.internal.util.annotation.ConstraintAnnotationDescriptor;
import org.hibernate.validator.testutil.TestForIssue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SafeHtmlValidator}.
 *
 * @author George Gastaldi
 * @author Hardy Ferentschik
 * @author Marko Bekhta
 */
public class SafeHtmlValidatorTest {

	private ConstraintAnnotationDescriptor.Builder<SafeHtml> descriptorBuilder;

	@BeforeMethod
	public void setUp() {
		descriptorBuilder = new ConstraintAnnotationDescriptor.Builder<>( SafeHtml.class );
	}

	@Test
	public void testNullValue() throws Exception {
		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.BASIC );

		assertTrue( getSafeHtmlValidator().isValid( null, null ) );
	}

	@Test
	public void testInvalidScriptTagIncluded() throws Exception {
		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.BASIC );

		assertFalse( getSafeHtmlValidator().isValid( "Hello<script>alert('Doh')</script>World !", null ) );
	}

	@Test
	public void testInvalidIncompleteImgTagWithScriptIncluded() {
		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.BASIC );

		assertFalse( getSafeHtmlValidator().isValid( "<img src=asdf onerror=\"alert(1)\" x=", null ) );
	}

	@Test
	public void testValid() throws Exception {
		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.BASIC );

		assertTrue( getSafeHtmlValidator().isValid( "<p><a href='http://example.com/'>Link</a></p>", null ) );
	}

	@Test
	public void testAdditionalTags() throws Exception {
		descriptorBuilder.setAttribute( "additionalTags", new String[] { "script" } );

		assertTrue( getSafeHtmlValidator().isValid( "Hello<script>alert('Doh')</script>World !", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-817")
	public void testDivNotAllowedInBasicWhiteList() throws Exception {
		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.BASIC );

		SafeHtmlValidator validator = getSafeHtmlValidator();
		assertFalse( validator.isValid( "<div>test</div>", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-817")
	public void testDivAllowedInRelaxedWhiteList() throws Exception {
		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.RELAXED );

		assertTrue( getSafeHtmlValidator().isValid( "<div>test</div>", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-817")
	public void testDivWithWhiteListedClassAttribute() throws Exception {
		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.RELAXED );

		AnnotationDescriptor.Builder<SafeHtml.Tag> tagDescriptorBuilder = new AnnotationDescriptor.Builder<>( SafeHtml.Tag.class );
		tagDescriptorBuilder.setAttribute( "name", "div" );
		tagDescriptorBuilder.setAttribute( "attributes", new String[] { "class" } );
		SafeHtml.Tag tag = tagDescriptorBuilder.build().getAnnotation();
		descriptorBuilder.setAttribute( "additionalTagsWithAttributes", new SafeHtml.Tag[] { tag } );

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
		assertNoViolations( constraintViolations );

		// the attributes are optional - allowing <div class> also allows just <div>
		constraintViolations = validator.validate( new Foo( "<div>test</div>" ) );
		assertNoViolations( constraintViolations );

		constraintViolations = validator.validate( new Foo( "<div class='foo'>test</div>" ) );
		assertThat( constraintViolations ).containsOnlyViolations(
				violationOf( SafeHtml.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-873")
	public void testValidationOfInvalidFragment() throws Exception {
		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.NONE );

		assertFalse( getSafeHtmlValidator().isValid( "<td>1234qwer</td>", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-873")
	public void testValidationOfValidFragment() throws Exception {
		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.RELAXED );

		assertTrue( getSafeHtmlValidator().isValid( "<td>1234qwer</td>", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-873")
	public void testValidationOfTextFragment() throws Exception {
		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.NONE );

		assertTrue( getSafeHtmlValidator().isValid( "Foobar", null ) );
	}

	@Test
	@TestForIssue(jiraKey = "HV-1302")
	public void testAdditionalProtocols() {
		Validator validator = getValidator();

		assertNoViolations( validator.validate( new Bar( "<img src='data:image/png;base64,100101' />" ) ) );
		assertNoViolations( validator.validate( new Bar( "<img/>" ) ) );
		assertThat( validator.validate( new Bar( "<img src='not_data:image/png;base64,100101' />" ) ) )
				.containsOnlyViolations(
						violationOf( SafeHtml.class )
				);
		assertThat( validator.validate( new Bar( "<img not_src='data:image/png;base64,100101' />" ) ) )
				.containsOnlyViolations(
						violationOf( SafeHtml.class )
				);
		assertThat( validator.validate( new Bar( "<div src='data:image/png;base64,100101' />" ) ) )
				.containsOnlyViolations(
						violationOf( SafeHtml.class )
				);
		assertThat( validator.validate( new Bar( "<div src='data:image/png;base64,100101' />" ) ) )
				.containsOnlyViolations(
						violationOf( SafeHtml.class )
				);
		assertNoViolations( validator.validate( new Bar(
				"<custom>" +
						"  <img src='data:image/png;base64,100101' />" +
						"  <custom attr1='strange_protocol:some_text' />" +
						"  <custom attr3='some_protocol:some_text' />" +
						"  <custom><img /></custom>" +
						"  <section id='sec1' attr='val'></section>" +
						"  <custom attr1='dataprotocol:some_text' attr2='strange_protocol:some_text' />" +
						"</custom>" ) ) );
		assertThat( validator.validate( new Bar(
				"<div>" +
						"<img src='not_data:image/png;base64,100101' />" +
						"<custom attr1='strange_protocol:some_text' />" +
						"/<div>"
		) ) ).containsOnlyViolations(
				violationOf( SafeHtml.class )
		);
	}

	@Test
	@TestForIssue(jiraKey = "HV-1303")
	public void testPreserveRelativeLinks() throws Exception {
		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.RELAXED );
		descriptorBuilder.setAttribute( "baseURI", "http://127.0.0.1" );

		assertTrue( getSafeHtmlValidator().isValid( "<img src='/some/relative/url/image.png' />", null ) );

		descriptorBuilder.setAttribute( "whitelistType", WhiteListType.RELAXED );
		descriptorBuilder.setAttribute( "baseURI", "" );

		assertFalse( getSafeHtmlValidator().isValid( "<img src='/some/relative/url/image.png' />", null ) );
	}

	private SafeHtmlValidator getSafeHtmlValidator() {
		SafeHtml p = descriptorBuilder.build().getAnnotation();
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

	public static class Bar {
		@SafeHtml(
				whitelistType = WhiteListType.BASIC,
				additionalTagsWithAttributes = {
						@SafeHtml.Tag(name = "img", attributesWithProtocols = @SafeHtml.Attribute(name = "src", protocols = { "data" })),
						@SafeHtml.Tag(name = "custom", attributesWithProtocols = {
								@SafeHtml.Attribute(name = "attr1", protocols = { "dataprotocol", "strange_protocol" }),
								@SafeHtml.Attribute(name = "attr2", protocols = { "dataprotocol", "strange_protocol" }),
								@SafeHtml.Attribute(name = "attr3", protocols = "some_protocol")
						}),
						@SafeHtml.Tag(name = "section", attributes = { "attr", "id" })
				}
		)
		String source;

		public Bar(String source) {
			this.source = source;
		}
	}
}
