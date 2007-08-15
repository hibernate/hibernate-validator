//$Id: $
package org.hibernate.validator;

/**
 * Responsible for validator message interpolation (variable replacement etc)
 * this extension point is useful if the call has some contextual informations to
 * interpolate in validator messages
 *
 * @author Emmanuel Bernard
 */
public interface MessageInterpolator {
	/**
	 * Interpolate a given validator message.
	 * The implementation is free to delegate to the default interpolator or not.
	 */
	String interpolate(String message, Validator validator, MessageInterpolator defaultInterpolator);
}
