/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.util;

import javax.tools.Diagnostic.Kind;

/**
 * Expectation value to be matched against a given {@link javax.tools.Diagnostic}.
 *
 * @author Gunnar Morling
 */
public class DiagnosticExpectation implements Comparable<DiagnosticExpectation> {

	private final Kind kind;

	private final long lineNumber;

	private final String message;

	public DiagnosticExpectation(Kind kind, long lineNumber) {
		this.kind = kind;
		this.lineNumber = lineNumber;
		this.message = null;
	}

	public DiagnosticExpectation(Kind kind, long lineNumber, String message) {
		this.kind = kind;
		this.lineNumber = lineNumber;
		this.message = message;
	}

	public Kind getKind() {
		return kind;
	}

	public long getLineNumber() {
		return lineNumber;
	}

	@Override
	public String toString() {
		return "DiagnosticExpectation [kind=" + kind + ", lineNumber=" + lineNumber + ", message=" + message + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ( ( kind == null ) ? 0 : kind.hashCode() );
		result = prime * result + (int) ( lineNumber ^ ( lineNumber >>> 32 ) );
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
		DiagnosticExpectation other = (DiagnosticExpectation) obj;
		if ( kind != other.kind ) {
			return false;
		}
		if ( lineNumber != other.lineNumber ) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(DiagnosticExpectation o) {
		if ( o == null ) {
			return 1;
		}
		if ( lineNumber == o.getLineNumber() ) {
			return kind.compareTo( o.getKind() );
		}
		else {
			return (int) ( lineNumber - o.getLineNumber() );
		}
	}
}
