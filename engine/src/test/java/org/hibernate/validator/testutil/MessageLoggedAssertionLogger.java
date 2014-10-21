/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.testutil;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import static org.testng.Assert.assertTrue;

/**
 * A log4j logger which can be used to assert that a specified message got logged.
 *
 * @author Hardy Ferentschik
 */
public class MessageLoggedAssertionLogger extends AppenderSkeleton {
	private final String expectedMessageCode;
	private boolean messageLogged;

	public MessageLoggedAssertionLogger(String expectedMessageCode) {
		this.expectedMessageCode = expectedMessageCode;
	}

	@Override
	protected void append(LoggingEvent event) {
		if ( event.getRenderedMessage().startsWith( expectedMessageCode ) ) {
			messageLogged = true;
		}
	}

	public void close() {
	}

	public boolean requiresLayout() {
		return false;
	}

	public void assertMessageLogged() {
		assertTrue( messageLogged, "Message " + expectedMessageCode + " got never logged" );
	}
}


