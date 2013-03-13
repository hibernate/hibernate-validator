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
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class LoadClass implements PrivilegedAction<Class<?>> {

	private static final Log log = LoggerFactory.make();

	private static final String HIBERNATE_VALIDATOR_CLASS_NAME = "org.hibernate.validator";

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
		if ( className.startsWith( HIBERNATE_VALIDATOR_CLASS_NAME ) ) {
			return loadClassInValidatorNameSpace();
		}
		else {
			return loadNonValidatorClass();
		}
	}

	// HV-363 - library internal classes are loaded via Class.forName first

	private Class<?> loadClassInValidatorNameSpace() {
		try {
			return Class.forName( className, true, caller.getClassLoader() );
		}
		catch ( ClassNotFoundException e ) {
			//ignore -- try using the class loader of context first
		}
		catch ( RuntimeException e ) {
			// ignore
		}
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if ( contextClassLoader != null ) {
				return Class.forName( className, false, contextClassLoader );
			}
			else {
				throw log.getUnableToLoadClassException( className );
			}
		}
		catch ( ClassNotFoundException e ) {
			throw log.getUnableToLoadClassException( className, e );
		}
	}

	private Class<?> loadNonValidatorClass() {
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if ( contextClassLoader != null ) {
				return Class.forName( className, false, contextClassLoader );
			}
		}
		catch ( ClassNotFoundException e ) {
			// ignore - try using the classloader of the caller first
		}
		catch ( RuntimeException e ) {
			// ignore
		}
		try {
			return Class.forName( className, true, caller.getClassLoader() );
		}
		catch ( ClassNotFoundException e ) {
			throw log.getUnableToLoadClassException( className, e );
		}
	}
}
