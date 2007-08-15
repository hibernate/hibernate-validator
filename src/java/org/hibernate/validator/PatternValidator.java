//$Id$
package org.hibernate.validator;

import java.io.Serializable;
import java.util.regex.Matcher;

/**
 * check if a given element match the regular expression
 *
 * @author Gavin King
 */
public class PatternValidator implements Validator<Pattern>, Serializable {

	private java.util.regex.Pattern pattern;

	public void initialize(Pattern parameters) {
		pattern = java.util.regex.Pattern.compile(
				parameters.regex(),
				parameters.flags()
		);
	}

	public boolean isValid(Object value) {
		if ( value == null ) return true;
		if ( !( value instanceof String ) ) return false;
		String string = (String) value;
		Matcher m = pattern.matcher( string );
		return m.matches();
	}

}
