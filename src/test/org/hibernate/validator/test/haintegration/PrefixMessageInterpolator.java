//$Id: $
package org.hibernate.validator.test.haintegration;

import org.hibernate.validator.MessageInterpolator;
import org.hibernate.validator.Validator;

/**
 * @author Emmanuel Bernard
 */
public class PrefixMessageInterpolator implements MessageInterpolator {
	public String interpolate(String message, Validator validator, MessageInterpolator defaultInterpolator) {
		return "prefix_" + defaultInterpolator.interpolate( message, validator, defaultInterpolator );
	}
}
