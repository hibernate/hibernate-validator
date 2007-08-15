//$Id: $
package org.hibernate.validator;

import java.util.List;
import java.util.ArrayList;

/**
 * Implement the Luhn algorithm (with Luhn key on the last digit)
 * @author Emmanuel Bernard
 */
public abstract class AbstractLuhnValidator {
	abstract int multiplicator();

	public boolean isValid(Object value) {
		if (value == null) return true;
		if ( ! ( value instanceof String) ) return false;
		String creditCard = (String) value;
		char[] chars = creditCard.toCharArray();

		List<Integer> ints = new ArrayList<Integer>();
		for (char c : chars) {
			if ( Character.isDigit( c ) ) ints.add( c - '0' );
		}
		int length = ints.size();
		int sum = 0;
		boolean even = false;
		for ( int index = length - 1 ; index >= 0 ; index-- ) {
			int digit = ints.get(index);
			if  (even) {
				digit *= multiplicator();
			}
			if (digit > 9) {
				digit = digit / 10 + digit % 10;
			}
			sum+= digit;
			even = !even;
		}
		return  sum % 10 == 0;
	}
}
