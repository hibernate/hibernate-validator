/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
		return Collections.unmodifiableList( this.events );
	}
}
