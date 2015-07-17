/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import javax.validation.MessageInterpolator;

/**
 * Resolves a given term.
 * 
 * @author Adam Stawicki
 */
public interface TermResolver {

	/**
	 * Interpolates given term based on the constraint validation context.
	 * 
	 * @param term the message to interpolate
	 * @param context contextual information related to the interpolation
	 * 
	 * @return interpolated message
	 */
	String interpolate(MessageInterpolator.Context context, String term);
}
