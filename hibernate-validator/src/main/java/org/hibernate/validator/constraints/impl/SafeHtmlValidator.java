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

import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.util.ReflectionHelper;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Validate that the string does not contain malicious code.
 * 
 * It uses JSoup (http://www.jsoup.org) as the underlying parser/sanitizer library.
 * 
 * @author George Gastaldi
 */
public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, CharSequence> {

	private Whitelist whitelist;

	public void initialize(SafeHtml constraintAnn) {
		Class<? extends Whitelist> whiteListClass = constraintAnn.whiteListClass();
		if ( whiteListClass != Whitelist.class ) {
			whitelist = ReflectionHelper.newInstance( whiteListClass, whiteListClass.getName() );
		}
		else {
			whitelist = constraintAnn.value().getWhitelist();
		}
	}

	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if ( value == null ) {
			return true;
		}
		return Jsoup.isValid( value.toString(), whitelist );
	}
}
