package org.hibernate.validation.util;

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