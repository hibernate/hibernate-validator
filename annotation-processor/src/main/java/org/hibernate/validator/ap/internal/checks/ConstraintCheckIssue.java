/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.Arrays;

/**
 * The result of the execution of a {@link ConstraintCheck}. Comprises
 * information about the location at which the issue occurred and a message
 * describing the occurred issue.
 *
 * @author Gunnar Morling
 */
public class ConstraintCheckIssue {

	public enum IssueKind {
		WARNING,
		ERROR,
	}

	private final Element element;

	private final AnnotationMirror annotationMirror;

	private final IssueKind kind;

	private final String messageKey;

	private final Object[] messageParameters;

	/**
	 * Creates a new ConstraintCheckIssue.
	 *
	 * @param element The element at which the error occurred.
	 * @param annotationMirror The annotation that causes the error.
	 * @param messageKey A key for retrieving an error message template from the bundle
	 * <p>
	 * {@code org.hibernate.validator.ap.ValidationProcessorMessages.}
	 * </p>
	 *
	 * @param messageParameters An array with values to put into the error message template
	 * using {@link java.text.MessageFormat}. The number of elements must match
	 * the number of place holders in the message template.
	 */
	public ConstraintCheckIssue(Element element, AnnotationMirror annotationMirror, IssueKind kind, String messageKey, Object... messageParameters) {
		this.element = element;
		this.annotationMirror = annotationMirror;
		this.kind = kind;
		this.messageKey = messageKey;
		this.messageParameters = messageParameters;
	}

	public Element getElement() {
		return element;
	}

	public AnnotationMirror getAnnotationMirror() {
		return annotationMirror;
	}

	public IssueKind getKind() {
		return kind;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public Object[] getMessageParameters() {
		return messageParameters;
	}

	/**
	 * Determine if issue is an error
	 *
	 * @return true if {@link ConstraintCheckIssue#getKind()} equals to {@link IssueKind#ERROR}
	 */
	public boolean isError() {
		return IssueKind.ERROR.equals( kind );
	}

	/**
	 * Determine if issue is a warning
	 *
	 * @return true if {@link ConstraintCheckIssue#getKind()} equals to {@link IssueKind#WARNING}
	 */
	public boolean isWarning() {
		return IssueKind.WARNING.equals( kind );
	}

	/**
	 * Creates a new ConstraintCheckIssue of error kind ({@link IssueKind#ERROR}).
	 *
	 * @param element The element at which the error occurred.
	 * @param annotationMirror The annotation that causes the error.
	 * @param messageKey A key for retrieving an error message template from the bundle
	 * <p>
	 * {@code org.hibernate.validator.ap.ValidationProcessorMessages.}
	 * </p>
	 * @param messageParameters An array with values to put into the error message template
	 * using {@link java.text.MessageFormat}. The number of elements must match
	 * the number of place holders in the message template.
	 */
	public static ConstraintCheckIssue error(Element element, AnnotationMirror annotationMirror, String messageKey, Object... messageParameters) {
		return new ConstraintCheckIssue( element, annotationMirror, IssueKind.ERROR, messageKey, messageParameters );
	}

	/**
	 * Creates a new ConstraintCheckIssue of warning kind ({@link IssueKind#WARNING}).
	 *
	 * @param element The element at which the error occurred.
	 * @param annotationMirror The annotation that causes the error.
	 * @param messageKey A key for retrieving an error message template from the bundle
	 * <p>
	 * {@code org.hibernate.validator.ap.ValidationProcessorMessages.}
	 * </p>
	 * @param messageParameters An array with values to put into the error message template
	 * using {@link java.text.MessageFormat}. The number of elements must match
	 * the number of place holders in the message template.
	 */
	public static ConstraintCheckIssue warning(Element element, AnnotationMirror annotationMirror, String messageKey, Object... messageParameters) {
		return new ConstraintCheckIssue( element, annotationMirror, IssueKind.WARNING, messageKey, messageParameters );
	}

	@Override
	public String toString() {
		return "ConstraintCheckIssue [annotationMirror=" + annotationMirror
				+ ", element=" + element + ", kind=" + kind + ", messageKey=" + messageKey
				+ ", messageParameters=" + Arrays.toString( messageParameters )
				+ "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ( ( annotationMirror == null ) ? 0 : annotationMirror.hashCode() );
		result = prime * result + ( ( element == null ) ? 0 : element.hashCode() );
		result = prime * result + ( ( kind == null ) ? 0 : kind.hashCode() );
		result = prime * result
				+ ( ( messageKey == null ) ? 0 : messageKey.hashCode() );
		result = prime * result + Arrays.hashCode( messageParameters );
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		ConstraintCheckIssue other = (ConstraintCheckIssue) obj;
		if ( annotationMirror == null ) {
			if ( other.annotationMirror != null ) {
				return false;
			}
		}
		else if ( !annotationMirror.equals( other.annotationMirror ) ) {
			return false;
		}
		if ( element == null ) {
			if ( other.element != null ) {
				return false;
			}
		}
		else if ( !element.equals( other.element ) ) {
			return false;
		}

		if ( kind == null ) {
			if ( other.kind != null ) {
				return false;
			}
		}
		else if ( !kind.equals( other.kind ) ) {
			return false;
		}
		if ( messageKey == null ) {
			if ( other.messageKey != null ) {
				return false;
			}
		}
		else if ( !messageKey.equals( other.messageKey ) ) {
			return false;
		}
		if ( !Arrays.equals( messageParameters, other.messageParameters ) ) {
			return false;
		}
		return true;
	}
}
