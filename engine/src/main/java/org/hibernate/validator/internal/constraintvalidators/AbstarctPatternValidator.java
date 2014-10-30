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

import java.util.regex.PatternSyntaxException;

import javax.validation.ConstraintValidator;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Provides common functionality for various {@link Pattern} validations.
 * 
 * @author Hardy Ferentschik
 * @author Radoslaw Smogura
 */
public abstract class AbstarctPatternValidator<T> implements ConstraintValidator<Pattern, T> {

	private static final Log log = LoggerFactory.make();

	private java.util.regex.Pattern pattern;

	public void initialize(Pattern parameters) {
		Pattern.Flag[] flags = parameters.flags();
		int intFlag = 0;
		for ( Pattern.Flag flag : flags ) {
			intFlag = intFlag | flag.getValue();
		}

		try {
			pattern = java.util.regex.Pattern.compile( parameters.regexp(), intFlag );
		}
		catch (PatternSyntaxException e) {
			throw log.getInvalidRegularExpressionException( e );
		}
	}

	/**
	 * Returns pattern used for validating constraint.
	 * 
	 * @return pattern used for validating constraint
	 */
	public java.util.regex.Pattern getPattern() {
		return pattern;
	}
}
