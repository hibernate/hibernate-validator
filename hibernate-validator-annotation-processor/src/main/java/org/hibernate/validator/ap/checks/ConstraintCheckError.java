/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.ap.checks;

import java.util.Arrays;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

/**
 * The result of the execution of a {@link ConstraintCheck}. Comprises
 * information about the location at which the error occurred and a message
 * describing the occurred error.
 *
 * @author Gunnar Morling
 */
public class ConstraintCheckError {

	private final Element element;

	private final AnnotationMirror annotationMirror;

	private final String messageKey;

	private final Object[] messageParameters;

	/**
	 * Creates a new ConstraintCheckError.
	 *
	 * @param element The element at which the error occurred.
	 * @param annotationMirror The annotation that causes the error.
	 * @param messageKey A key for retrieving an error message template from the bundle
	 * <p/>
	 * <code>org.hibernate.validator.ap.ValidationProcessorMessages.</code>
	 * @param messageParameters An array with values to put into the error message template
	 * using {@link java.text.MessageFormat}. The number of elements must match
	 * the number of place holders in the message template.
	 */
	public ConstraintCheckError(Element element,
								AnnotationMirror annotationMirror, String messageKey, Object... messageParameters) {

		this.element = element;
		this.annotationMirror = annotationMirror;
		this.messageKey = messageKey;
		this.messageParameters = messageParameters;
	}

	public Element getElement() {
		return element;
	}

	public AnnotationMirror getAnnotationMirror() {
		return annotationMirror;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public Object[] getMessageParameters() {
		return messageParameters;
	}

	@Override
	public String toString() {
		return "ConstraintCheckError [annotationMirror=" + annotationMirror
				+ ", element=" + element + ", messageKey=" + messageKey
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
		ConstraintCheckError other = (ConstraintCheckError) obj;
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
