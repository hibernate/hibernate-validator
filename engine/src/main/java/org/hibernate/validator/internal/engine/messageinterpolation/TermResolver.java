/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.messageinterpolation;

import java.util.Locale;

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
	 * @param context contextual information related to the interpolation
	 * @param locale the locale for which to interpolate the expression
	 * @param term the message to interpolate
	 * @return interpolated message
	 */
	String interpolate(MessageInterpolator.Context context, Locale locale, String term);
}
