/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
