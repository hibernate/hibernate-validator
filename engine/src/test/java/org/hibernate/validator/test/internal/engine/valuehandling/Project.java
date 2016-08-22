/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.internal.engine.valuehandling;

import java.time.LocalDate;
import java.util.Optional;
import javax.validation.constraints.AssertTrue;

/**
 * @author Hardy Ferentschik
 */
public class Project {
	private final LocalDate start;
	private final LocalDate end;

	public Project(LocalDate start, LocalDate end) {
		this.start = start;
		this.end = end;
	}

	public Optional<LocalDate> getStart() {
		return Optional.ofNullable( this.start );
	}

	public Optional<LocalDate> getEnd() {
		return Optional.ofNullable( this.end );
	}

	@AssertTrue
	private Boolean getStartBeforeEnd() {
		return this.getStart().orElse( LocalDate.MAX ).isBefore( this.getEnd().orElse( LocalDate.MIN ) );
	}
}
