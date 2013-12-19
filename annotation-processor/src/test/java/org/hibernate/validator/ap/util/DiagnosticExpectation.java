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

import javax.tools.Diagnostic.Kind;

/**
 * Expectation value to be matched against a given {@link javax.tools.Diagnostic}.
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		result = prime * result + (int) (lineNumber ^ (lineNumber >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiagnosticExpectation other = (DiagnosticExpectation) obj;
		if (kind != other.kind)
			return false;
		if (lineNumber != other.lineNumber)
			return false;
		return true;
	}
}
