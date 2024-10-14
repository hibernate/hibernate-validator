/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import jakarta.validation.MessageInterpolator;

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
