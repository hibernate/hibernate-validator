/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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


