//$Id: $
package org.hibernate.validator;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.hibernate.mapping.Property;
import org.hibernate.mapping.Column;

/**
 * Validate a Digit to check if it matches the according pattern
 *
 * @author Norman Richards
 * @author Emmanuel Bernard
 */
public class DigitsValidator implements Validator<Digits>, PropertyConstraint {
	int integerDigits;
	int fractionalDigits;

	public void initialize(Digits configuration) {
		integerDigits = configuration.integerDigits();
		fractionalDigits = configuration.fractionalDigits();
	}

	public boolean isValid(Object value) {
		if ( value == null ) {
			return true;
		}

		String stringValue = null;

		if ( value instanceof String ) {
			try {
				stringValue = stringValue( new BigDecimal( (String) value ) );
			}
			catch (NumberFormatException nfe) {
				return false;
			}
		}
		else if ( value instanceof BigDecimal ) {
			stringValue = stringValue( (BigDecimal) value );
		}
		else if ( value instanceof BigInteger ) {
			stringValue = stringValue( (BigInteger) value );
		}
		else if ( value instanceof Number ) {
			//yukky
			stringValue = stringValue( new BigDecimal( ( (Number) value ).toString() ) );
		}
		else {
			return false;
		}

		int pos = stringValue.indexOf( "." );

		int left = ( pos == -1 ) ?
				stringValue.length() :
				pos;
		int right = ( pos == -1 ) ?
				0 :
				stringValue.length() - pos - 1;

		if ( left == 1 && stringValue.charAt( 0 ) == '0' ) {
			left--;
		}

		return !( left > integerDigits || right > fractionalDigits );

	}

	private String stringValue(BigDecimal number) {
		return number.abs().stripTrailingZeros().toPlainString();
	}

	private String stringValue(BigInteger number) {
		return number.abs().toString();
	}

	public void apply(Property property) {
		Column col = (Column) property.getColumnIterator().next();
		col.setPrecision( integerDigits + fractionalDigits );
		col.setScale( fractionalDigits );
	}
}
