/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package com.example;

import java.time.LocalDate;

import org.hibernate.validator.constraints.ScriptAssert;

/**
 * @author Marko Bekhta
 */
@ScriptAssert(lang = "groovy", script = "_this.start < _this.end", message = "start of event cannot be after the end")
public class Event {

	private final LocalDate start;

	private final LocalDate end;

	public Event(LocalDate start, LocalDate end) {
		this.start = start;
		this.end = end;
	}

	@ScriptAssert(lang = "groovy", script = "true", message = "groovy message")
	@ScriptAssert(lang = "ECMAScript", script = "true", message = "ECMAScript message")
	public static class EventLocation {

	}
}
