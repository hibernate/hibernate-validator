/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

import java.security.PrivilegedAction;

/**
 * Loads a class specified by name.
 * <p>
 * If no class loader is provided, first the thread context class loader is tried, and finally Hibernate Validator's own
 * class loader.
 * <p>
 * <b>Note</b>: When loading classes provided by the user (such as XML-configured beans or constraint types), the user
 * class loader passed to the configuration must be passed.
 *
 * @author Emmanuel Bernard
 * @author Hardy Ferentschik
 * @author Kevin Pollet &lt;kevin.pollet@serli.com&gt; (C) 2011 SERLI
 * @author Gunnar Morling
 */
public final class LoadClass implements PrivilegedAction<Class<?>> {

	private static final Log log = LoggerFactory.make();

	private static final String HIBERNATE_VALIDATOR_CLASS_NAME = "org.hibernate.validator";

	private final String className;

	private final ClassLoader classLoader;

	public static LoadClass action(String className, ClassLoader classLoader) {
		return new LoadClass( className, classLoader );
	}

	private LoadClass(String className, ClassLoader classLoader) {
		this.className = className;
		this.classLoader = classLoader;
	}

	@Override
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
			return Class.forName( className, true, HibernateValidator.class.getClassLoader() );
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
			if ( classLoader != null ) {
				return Class.forName( className, false, classLoader );
			}
		}
		catch ( ClassNotFoundException e ) {
			// ignore - try using the classloader of the caller first
		}
		catch ( RuntimeException e ) {
			// ignore
		}
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
			return Class.forName( className, true, LoadClass.class.getClassLoader() );
		}
		catch ( ClassNotFoundException e ) {
			throw log.getUnableToLoadClassException( className, e );
		}
	}
}
