/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
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
