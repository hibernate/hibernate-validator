/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hibernate.validator.constraints.SafeHtml.WhiteListType.RELAXED;

/**
 * Validate a rich text value provided by the user to ensure that it contains no malicious code, such as embedded
 * &lt;script&gt; elements.
 * <p>
 * Note that this constraint assumes you want to validate input which represents a body fragment of an HTML document. If
 * you instead want to validate input which represents a complete HTML document, add the {@code html}, {@code head} and
 * {@code body} tags to the used whitelist as required.
 *
 * @author George Gastaldi
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface SafeHtml {

	String message() default "{org.hibernate.validator.constraints.SafeHtml.message}";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };

	/**
	 * @return The built-in whitelist type which will be applied to the rich text value
	 */
	WhiteListType whitelistType() default RELAXED;

	/**
	 * @return Additional whitelist tags which are allowed on top of the tags specified by the
	 * {@link #whitelistType()}.
	 */
	String[] additionalTags() default { };

	/**
	 * @return Allows to specify additional whitelist tags with optional attributes.
	 */
	Tag[] additionalTagsWithAttributes() default { };

	/**
	 * Allows to specify whitelist tags with specified optional attributes. Adding a tag with a given attribute also
	 * whitelists the tag itself without any attribute.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface Tag {
		/**
		 * @return the tag name to whitelist.
		 */
		String name();

		/**
		 * @return list of tag attributes which are whitelisted.
		 */
		String[] attributes() default { };
	}

	/**
	 * Defines several {@code @SafeHtml} annotations on the same element.
	 */
	@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
	@Retention(RUNTIME)
	@Documented
	public @interface List {
		SafeHtml[] value();
	}

	/**
	 * Defines default whitelist implementations.
	 */
	public enum WhiteListType {
		/**
		 * This whitelist allows only text nodes: all HTML will be stripped.
		 */
		NONE,

		/**
		 * This whitelist allows only simple text formatting: <code>b, em, i, strong, u</code>. All other HTML (tags and
		 * attributes) will be removed.
		 */
		SIMPLE_TEXT,

		/**
		 * This whitelist allows a fuller range of text nodes:
		 * <code>a, b, blockquote, br, cite, code, dd, dl, dt, em, i, li, ol, p, pre, q, small, strike, strong, sub, sup, u, ul</code>
		 * , and appropriate attributes.
		 * <p/>
		 * Links (<code>a</code> elements) can point to <code>http, https, ftp, mailto</code>, and have an enforced
		 * <code>rel=nofollow</code> attribute.
		 * <p/>
		 * Does not allow images.
		 */
		BASIC,

		/**
		 * This whitelist allows the same text tags as {@link WhiteListType#BASIC}, and also allows <code>img</code>
		 * tags,
		 * with
		 * appropriate attributes, with <code>src</code> pointing to <code>http</code> or <code>https</code>.
		 */
		BASIC_WITH_IMAGES,

		/**
		 * This whitelist allows a full range of text and structural body HTML:
		 * <code>a, b, blockquote, br, caption, cite, code, col, colgroup, dd, dl, dt, em, h1, h2, h3, h4, h5, h6, i, img, li,
		 * ol, p, pre, q, small, strike, strong, sub, sup, table, tbody, td, tfoot, th, thead, tr, u, ul</code>
		 * <p/>
		 * Links do not have an enforced <code>rel=nofollow</code> attribute, but you can add that if desired.
		 */
		RELAXED
	}
}
