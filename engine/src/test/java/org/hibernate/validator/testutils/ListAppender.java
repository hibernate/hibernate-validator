/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.MutableLogEvent;


/**
 * A simple log appender for tests.
 *
 * @author Marko Bekhta
 */
public class ListAppender extends AbstractAppender {
	final List<LogEvent> events = Collections.synchronizedList( new ArrayList<>() );
	private final List<String> messages = Collections.synchronizedList( new ArrayList() );
	final List<byte[]> data = Collections.synchronizedList( new ArrayList() );

	public ListAppender(final String name) {
		super( name, null, null, true, Property.EMPTY_ARRAY );
	}


	public void append(final LogEvent event) {
		if ( event instanceof MutableLogEvent ) {
			this.events.add( ( (MutableLogEvent) event ).createMemento() );
		}
		else {
			this.events.add( event );
		}
	}

	public void clear() {
		this.events.clear();
	}

	public List<LogEvent> getEvents() {
		return Collections.unmodifiableList( this.events );
	}
}
