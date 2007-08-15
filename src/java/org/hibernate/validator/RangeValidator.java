//$Id$
package org.hibernate.validator;

import java.io.Serializable;
import java.math.BigDecimal;

import org.hibernate.mapping.Column;
import org.hibernate.mapping.Property;

/**
 * The value has to be in a defined range, the constraint is also applied on DB
 *
 * @author Gavin King
 */
public class RangeValidator implements Validator<Range>, PropertyConstraint, Serializable {
	private long max;
	private long min;

	public void initialize(Range parameters) {
		max = parameters.max();
		min = parameters.min();
	}

	public boolean isValid(Object value) {
		if ( value == null ) return true;
		if ( value instanceof String ) {
			try {
				BigDecimal dv = new BigDecimal( (String) value );
				return dv.compareTo( BigDecimal.valueOf( min ) ) >= 0 && dv.compareTo( BigDecimal.valueOf( max ) ) <= 0;
			}
			catch (NumberFormatException nfe) {
				return false;
			}
		}
		else if ( ( value instanceof Double ) || ( value instanceof Float ) ) {
			double dv = ( (Number) value ).doubleValue();
			return dv >= min && dv <= max;
		}
		else if ( value instanceof Number ) {
			long lv = ( (Number) value ).longValue();
			return lv >= min && lv <= max;
		}
		else {
			return false;
		}
	}

	public void apply(Property property) {
		Column col = (Column) property.getColumnIterator().next();
		String check = "";
		if ( min != Long.MIN_VALUE ) check += col.getName() + ">=" + min;
		if ( max != Long.MAX_VALUE && min != Long.MIN_VALUE ) check += " and ";
		if ( max != Long.MAX_VALUE ) check += col.getName() + "<=" + max;
		col.setCheckConstraint( check );
	}

}
