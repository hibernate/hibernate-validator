/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * Execute instance creation as privileged action.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 */
public final class NewInstance<T> implements PrivilegedAction<T> {

	private static final Log log = LoggerFactory.make();

	private final Class<T> clazz;
	private final String message;

	public static <T> NewInstance<T> action(Class<T> clazz, String message) {
		return new NewInstance<T>( clazz, message );
	}

	private NewInstance(Class<T> clazz, String message) {
		this.clazz = clazz;
		this.message = message;
	}

	public T run() {
		try {
			return clazz.newInstance();
		}
		catch ( InstantiationException e ) {
			throw log.getUnableToInstantiateException( message, clazz, e );
		}
		catch ( IllegalAccessException e ) {
			throw log.getUnableToInstantiateException( clazz, e );
		}
		catch ( RuntimeException e ) {
			throw log.getUnableToInstantiateException( clazz, e );
		}
	}
}
