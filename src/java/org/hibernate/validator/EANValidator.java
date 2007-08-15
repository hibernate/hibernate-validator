//$Id: $
package org.hibernate.validator;

import java.util.List;
import java.util.ArrayList;

/**
 * Validate EAN13 and UPC-A
 *
 * @author Emmanuel Bernard
 */
public class EANValidator implements Validator<EAN> {

	public void initialize(EAN parameters) {
	}


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
				digit *= 3;
			}
			sum+= digit;
			even = !even;
		}
		return  sum % 10 == 0;
	}
}
