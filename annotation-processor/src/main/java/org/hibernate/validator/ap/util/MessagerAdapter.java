/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Set;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

import org.hibernate.validator.ap.checks.ConstraintCheckError;

/**
 * Wrapper around {@link Messager}, which adds the ability to format error messages using {@link MessageFormat}.
 *
 * @author Gunnar Morling
 */
public class MessagerAdapter {

	/**
	 * Contains the texts to be displayed.
	 */
	private final ResourceBundle errorMessages;

	private final Messager messager;

	/**
	 * The kind of diagnostic to be used when reporting any problems.
	 */
	private Kind diagnosticKind;

	/**
	 * Creates a new MessagerAdapter.
	 *
	 * @param messager The underlying messager.
	 * @param diagnosticKind The kind with which messages shall be reported.
	 */
	public MessagerAdapter(Messager messager, Kind diagnosticKind) {

		this.messager = messager;
		this.diagnosticKind = diagnosticKind;

		errorMessages = ResourceBundle.getBundle( "org.hibernate.validator.ap.ValidationProcessorMessages" );
	}

	/**
	 * Returns the messager used by this adapter.
	 *
	 * @return The underlying messager.
	 */
	public Messager getDelegate() {
		return messager;
	}

	/**
	 * Reports the given errors against the underlying {@link Messager} using
	 * the specified {@link Kind}.
	 *
	 * @param errors A set with errors to report. May be empty but must not be
	 * null.
	 */
	public void reportErrors(Set<ConstraintCheckError> errors) {
		for ( ConstraintCheckError oneError : errors ) {
			reportError( oneError );
		}
	}

	/**
	 * Reports the given error. Message parameters will be put into the template
	 * retrieved from the resource bundle if applicable.
	 *
	 * @param error The error to report.
	 */
	private void reportError(ConstraintCheckError error) {

		String message = errorMessages.getString( error.getMessageKey() );

		if ( error.getMessageParameters() != null ) {
			message = MessageFormat.format( message, error.getMessageParameters() );
		}

		messager.printMessage(
				diagnosticKind, message, error.getElement(), error.getAnnotationMirror()
		);
	}

}
