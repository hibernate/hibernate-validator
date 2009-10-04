// $Id$
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
package org.hibernate.validator.util;

import java.security.PrivilegedAction;
import javax.validation.ValidationException;

/**
 * @author Emmanuel Bernard
 */
public class LoadClass implements PrivilegedAction<Class<?>> {
	private final String className;
	private final Class<?> caller;

	public static LoadClass action(String className, Class<?> caller) {
		return new LoadClass( className, caller );
	}

	private LoadClass(String className, Class<?> caller) {
		this.className = className;
		this.caller = caller;
	}

	public Class<?> run() {
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if ( contextClassLoader != null ) {
				return contextClassLoader.loadClass( className );
			}
		}
		catch ( Throwable e ) {
			// ignore
		}
		try {
			return Class.forName( className, true, caller.getClassLoader() );
		}
		catch ( ClassNotFoundException e ) {
			throw new ValidationException("Unable to load class: " + className, e);
		}
	}
}
