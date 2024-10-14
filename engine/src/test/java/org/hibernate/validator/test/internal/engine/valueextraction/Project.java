/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.test.internal.engine.valueextraction;

import java.time.LocalDate;
import java.util.Optional;

import jakarta.validation.constraints.AssertTrue;

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
