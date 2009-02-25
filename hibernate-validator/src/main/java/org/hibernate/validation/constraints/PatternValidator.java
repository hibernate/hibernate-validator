// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.constraints;

import java.util.regex.Matcher;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraints.Pattern;

/**
 * @author Hardy Ferentschik
 */
public class PatternValidator implements ConstraintValidator<Pattern, String> {

	private java.util.regex.Pattern pattern;

	public void initialize(Pattern parameters) {
		Pattern.Flag flags[] = parameters.flags();
		if ( flags.length == 0 ) {
			pattern = java.util.regex.Pattern.compile( parameters.regexp() );
		}
		else {
			int intFlag = 0;
			for ( Pattern.Flag flag : flags ) {
				intFlag = intFlag | mapFlagToInt( flag );
			}
			pattern = java.util.regex.Pattern.compile( parameters.regexp(), intFlag );
		}
	}

	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		if ( value == null ) {
			return true;
		}
		Matcher m = pattern.matcher( value );
		return m.matches();
	}

	private int mapFlagToInt(Pattern.Flag flag) {
		int intFlag = 0;
		switch ( flag ) {
			case UNIX_LINES: {
				intFlag = java.util.regex.Pattern.UNIX_LINES;
				break;
			}
			case CASE_INSENSITIVE: {
				intFlag = java.util.regex.Pattern.CASE_INSENSITIVE;
				break;
			}
			case COMMENTS: {
				intFlag = java.util.regex.Pattern.COMMENTS;
				break;
			}
			case MULTILINE: {
				intFlag = java.util.regex.Pattern.MULTILINE;
				break;
			}
			case DOTALL: {
				intFlag = java.util.regex.Pattern.DOTALL;
				break;
			}
			case UNICODE_CASE: {
				intFlag = java.util.regex.Pattern.UNICODE_CASE;
				break;
			}
			case CANON_EQ: {
				intFlag = java.util.regex.Pattern.CANON_EQ;
				break;
			}
		}
		return intFlag;
	}
}
