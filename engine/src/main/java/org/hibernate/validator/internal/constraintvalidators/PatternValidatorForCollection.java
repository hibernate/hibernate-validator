/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.constraintvalidators;

import java.util.Collection;
import java.util.regex.Matcher;

import javax.validation.ConstraintValidatorContext;

/**
 * @author Radoslaw Smogura
 */
public class PatternValidatorForCollection extends AbstarctPatternValidator<Collection<? extends CharSequence>> {

	/**
	 * Validates collection of {@link CharSequence}.
	 * 
	 * @return {@code true} iff collections is empty, or {@code null} and each not-null elements matches pattern
	 */
	public boolean isValid(Collection<? extends CharSequence> values, ConstraintValidatorContext constraintValidatorContext) {
		if ( values == null || values.isEmpty() ) {
			return true;
		}

		final java.util.regex.Pattern pattern = getPattern();
		for ( final CharSequence chars : values ) {
			if ( chars == null ) {
				continue;
			}

			final Matcher m = pattern.matcher( chars );
			if ( !m.matches() ) {
				return false;
			}
		}

		return true;
	}
}
