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
package org.hibernate.validator.constraints.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import org.hibernate.validator.constraints.SafeHtml;

/**
 * Validate that the string does not contain malicious code.
 *
 * It uses <a href="http://www.jsoup.org">JSoup</a> as the underlying parser/sanitizer library.
 *
 * @author George Gastaldi
 */
public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, CharSequence> {
	private Whitelist whitelist;

	public void initialize(SafeHtml constraintAnn) {
		switch ( constraintAnn.whitelistType() ) {
			case BASIC:
				whitelist = Whitelist.basic();
				break;
			case BASIC_WITH_IMAGES:
				whitelist = Whitelist.basicWithImages();
				break;
			case NONE:
				whitelist = Whitelist.none();
				break;
			case RELAXED:
				whitelist = Whitelist.relaxed();
				break;
			case SIMPLE_TEXT:
				whitelist = Whitelist.simpleText();
				break;
		}
		whitelist.addTags( constraintAnn.additionalTags() );
	}

	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return Jsoup.isValid( value.toString(), whitelist );
	}
}
