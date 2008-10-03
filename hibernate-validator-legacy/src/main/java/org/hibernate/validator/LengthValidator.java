//$Id$
package org.hibernate.validator;

import java.io.Serializable;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.Property;

/**
 * Do check a length restriction on a string, and apply expected contraints on hibernate metadata.
 *
 * @author Gavin King
 */
public class LengthValidator implements Validator<Length>, PropertyConstraint, Serializable {
	private int max;
	private int min;

	public void initialize(Length parameters) {
		max = parameters.max();
		min = parameters.min();
	}

	public boolean isValid(Object value) {
		if ( value == null ) return true;
		if ( !( value instanceof String ) ) return false;
		String string = (String) value;
		int length = string.length();
		return length >= min && length <= max;
	}

	public void apply(Property property) {
		Column col = (Column) property.getColumnIterator().next();
		if ( max < Integer.MAX_VALUE ) col.setLength( max );
	}

}
