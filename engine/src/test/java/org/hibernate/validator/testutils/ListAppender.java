/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.testutils;

import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.apache.logging.log4j.message.Message;

/**
 * A simple log appender for tests.
 *
 * @author Marko Bekhta
 */
public class ListAppender extends AbstractAppender {
	final List<LogEvent> events = Collections.synchronizedList( new ArrayList<>() );

	public ListAppender(final String name) {
		super( name, null, null, true, Property.EMPTY_ARRAY );
	}

	public void append(final LogEvent event) {
		if ( event instanceof MutableLogEvent e ) {
			this.events.add( e.toImmutable() );
		}
		else {
			this.events.add( event );
		}
	}

	public void clear() {
		this.events.clear();
	}

	public List<LogEvent> getEvents() {
		return unmodifiableList( this.events );
	}

	/**
	 * Returns all logged messages
	 */
	public List<String> getMessages() {
		return getMessages( logEvent -> true );
	}

	/**
	 * Returns logged messages matching the specified {@link Level}
	 */
	public List<String> getMessages(Level level) {
		return getMessages( logEvent -> logEvent.getLevel().equals( level ) );
	}

	private List<String> getMessages(Predicate<LogEvent> filter) {
		return events.stream().filter( filter ).map( LogEvent::getMessage )
				.map( Message::getFormattedMessage ).toList();
	}
}
