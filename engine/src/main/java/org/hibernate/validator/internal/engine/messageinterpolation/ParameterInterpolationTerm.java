/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.util.Arrays;
import java.util.Locale;
import javax.validation.MessageInterpolator;

/**
 * Represents an interpolation term using simple Bean Validation parameter syntax.
 *
 * @author Hardy Ferentschik
 */
public class ParameterInterpolationTerm extends InterpolationTerm {

	public ParameterInterpolationTerm(String expression, Locale locale) {
		super( expression, locale );
	}

	@Override
	public String interpolate(MessageInterpolator.Context context) {
		String resolvedExpression;
		Object variable = context.getConstraintDescriptor()
				.getAttributes()
				.get( removeCurlyBraces( expression ) );
		if ( variable != null ) {
			if ( variable.getClass().isArray() ) {
				resolvedExpression = Arrays.toString( (Object[]) variable );
			}
			else {
				resolvedExpression = variable.toString();
			}
		}
		else {
			resolvedExpression = expression;
		}
		return leadingEscapeCharacters + resolvedExpression;
	}

	private String removeCurlyBraces(String parameter) {
		return parameter.substring( 1, parameter.length() - 1 );
	}
}
