/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.util;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;

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
	private final Kind diagnosticKind;

	/**
	 * Creates a new MessagerAdapter.
	 *
	 * @param messager The underlying messager.
	 * @param diagnosticKind The kind with which messages shall be reported.
	 */
	public MessagerAdapter(Messager messager, Kind diagnosticKind) {

		this.messager = messager;
		this.diagnosticKind = diagnosticKind;

		errorMessages = ResourceBundle.getBundle( "org.hibernate.validator.ap.ValidationProcessorMessages", Locale.getDefault() );
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
	public void reportErrors(Collection<ConstraintCheckIssue> errors) {
		for ( ConstraintCheckIssue error : errors ) {
			reportError( error );
		}
	}

	/**
	 * Reports the given error. Message parameters will be put into the template
	 * retrieved from the resource bundle if applicable.
	 *
	 * @param error The error to report.
	 */
	private void reportError(ConstraintCheckIssue error) {
		report( error, diagnosticKind );
	}

	/**
	 * Reports the given warnings against the underlying {@link Messager} using
	 * the specified {@link Kind}.
	 *
	 * @param warnings A set with errors to report. May be empty but must not be
	 * null.
	 */
	public void reportWarnings(Collection<ConstraintCheckIssue> warnings) {
		for ( ConstraintCheckIssue warning : warnings ) {
			reportWarning( warning );
		}
	}

	/**
	 * Reports the given warning. Message parameters will be put into the template
	 * retrieved from the resource bundle if applicable.
	 *
	 * @param warning The warning to report.
	 */
	private void reportWarning(ConstraintCheckIssue warning) {
		report( warning, Kind.WARNING );
	}

	/**
	 * Reports the given issue. Message parameters will be put into the template
	 * retrieved from the resource bundle if applicable.
	 *
	 * @param issue The issue to report.
	 * @param kind Kind of diagnostics to be used for reporting a given issue.
	 */
	private void report(ConstraintCheckIssue issue, Kind kind) {
		String message = errorMessages.getString( issue.getMessageKey() );

		if ( issue.getMessageParameters() != null ) {
			MessageFormat messageFormat = new MessageFormat( message, Locale.getDefault() );
			message = messageFormat.format( issue.getMessageParameters() );
		}

		messager.printMessage(
				kind, message, issue.getElement(), issue.getAnnotationMirror()
		);
	}

}
