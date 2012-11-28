/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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
import java.util.Map;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic.Kind;

/**
 * Provides access to the processor options supported by the Hibernate Validator
 * annotation processor.
 *
 * @author Gunnar Morling
 */
public class Configuration {

	/**
	 * The name of the processor option for setting the diagnostic kind to be
	 * used when reporting errors during annotation processing.
	 */
	public static final String DIAGNOSTIC_KIND_PROCESSOR_OPTION = "diagnosticKind";

	/**
	 * The name of the processor option for activating verbose message reporting.
	 */
	public static final String VERBOSE_PROCESSOR_OPTION = "verbose";

	/**
	 * The name of the processor option for allowing constraints at methods
	 * other than getter methods.
	 */
	public static final String METHOD_CONSTRAINTS_SUPPORTED_PROCESSOR_OPTION = "methodConstraintsSupported";

	/**
	 * The diagnostic kind to be used if no or an invalid kind is given as processor option.
	 */
	public static final Kind DEFAULT_DIAGNOSTIC_KIND = Kind.ERROR;

	private final Kind diagnosticKind;

	private final boolean verbose;

	private final boolean methodConstraintsSupported;

	public Configuration(Map<String, String> options, Messager messager) {

		this.diagnosticKind = getDiagnosticKindOption( options, messager );
		this.verbose = getVerboseOption( options, messager );
		this.methodConstraintsSupported = getMethodConstraintsSupportedOption( options );
	}

	/**
	 * Returns the diagnosticKind to be used when reporting failing constraint checks.
	 */
	public Kind getDiagnosticKind() {
		return diagnosticKind;
	}

	/**
	 * Whether logging information shall be put out in a verbose way or not.
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * Whether method constraints are allowed at any method (<code>true</code>) or only
	 * getter methods (<code>false</code>).
	 */
	public boolean methodConstraintsSupported() {
		return methodConstraintsSupported;
	}

	/**
	 * Retrieves the diagnostic kind to be used for error messages. If given in
	 * processor options, it will be taken from there, otherwise the default
	 * value {@link Kind#ERROR} will be returned.
	 */
	private Kind getDiagnosticKindOption(Map<String, String> options, Messager messager) {

		String diagnosticKindFromOptions = options.get( DIAGNOSTIC_KIND_PROCESSOR_OPTION );

		if ( diagnosticKindFromOptions != null ) {
			try {
				return Kind.valueOf( diagnosticKindFromOptions );
			}
			catch ( IllegalArgumentException e ) {
				messager.printMessage(
						Kind.WARNING, MessageFormat.format(
						"The given value {0} is no valid diagnostic kind. {1} will be used.",
						diagnosticKindFromOptions,
						DEFAULT_DIAGNOSTIC_KIND
				)
				);
			}
		}

		return DEFAULT_DIAGNOSTIC_KIND;
	}

	/**
	 * Retrieves the value for the "verbose" property from the options.
	 */
	private boolean getVerboseOption(Map<String, String> options, Messager messager) {

		boolean theValue = Boolean.parseBoolean( options.get( VERBOSE_PROCESSOR_OPTION ) );

		if ( theValue ) {
			messager.printMessage(
					Kind.NOTE, MessageFormat.format(
					"Verbose reporting is activated. Some processing information will be displayed using diagnostic kind {0}.",
					Kind.NOTE
			)
			);
		}

		return theValue;
	}

	/**
	 * Retrieves the value for the "methodConstraintsSupported" property from the options.
	 */
	private boolean getMethodConstraintsSupportedOption(Map<String, String> options) {

		String methodConstraintsSupported = options.get( METHOD_CONSTRAINTS_SUPPORTED_PROCESSOR_OPTION );

		//allow method constraints by default
		if ( methodConstraintsSupported == null ) {
			return true;
		}

		return Boolean.parseBoolean( methodConstraintsSupported );
	}
}
