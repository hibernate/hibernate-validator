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

import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;

/**
 * Expectation value to be matched against a given {@link Diagnostic}.
 *
 * @author Gunnar Morling
 */
public class DiagnosticExpectation {

	private final Kind kind;

	private final long lineNumber;

	public DiagnosticExpectation(Kind kind, long lineNumber) {
		this.kind = kind;
		this.lineNumber = lineNumber;
	}

	public Kind getKind() {
		return kind;
	}

	public long getLineNumber() {
		return lineNumber;
	}

	@Override
	public String toString() {
		return "DiagnosticExpectation [kind=" + kind + ", lineNumber=" + lineNumber + "]";
	}
}
