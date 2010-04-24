// $Id: MessagerAdapter.java 19033 2010-03-19 21:27:15Z gunnar.morling $
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
package org.hibernate.validator.ap.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

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
	 * Reports an error at the given location using the given message key and
	 * optionally the given message parameters.
	 *
	 * @param element The element at which the error shall be reported.
	 * @param annotation The annotation mirror at which the error shall be reported.
	 * @param messageKey The message key to be used to retrieve the text.
	 * @param messageParameters An optional array of message parameters to be put into the
	 * message using a {@link MessageFormat}.
	 */
	public void reportError(Element element, AnnotationMirror annotation, String messageKey, Object... messageParameters) {

		String message = errorMessages.getString( messageKey );

		if ( message != null &&
				messageParameters != null ) {

			message = MessageFormat.format( errorMessages.getString( messageKey ), messageParameters );
		}
		else {
			message = messageKey;
		}

		messager.printMessage(
				diagnosticKind, message, element, annotation
		);
	}

}
