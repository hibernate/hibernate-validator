/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetClassLoader;

/**
 * Helper methods for loading resource files
 *
 * @author Hardy Ferentschik
 */
final class ResourceLoaderHelper {
	private static final Log log = LoggerFactory.make();

	/**
	 * Returns an input stream for the given path, which supports the mark/reset
	 * contract.
	 *
	 * @param path The path of the requested input stream.
	 *
	 * @return An input stream for the given path or {@code null} if no such
	 *         resource exists.
	 *
	 * @see InputStream#markSupported()
	 */
	static InputStream getResettableInputStreamForPath(String path, ClassLoader externalClassLoader) {
		//TODO not sure if it's the right thing to removing '/'
		String inputPath = path;
		if ( inputPath.startsWith( "/" ) ) {
			inputPath = inputPath.substring( 1 );
		}

		InputStream inputStream = null;

		if ( externalClassLoader != null ) {
			log.debug( "Trying to load " + path + " via user class loader" );
			inputStream = externalClassLoader.getResourceAsStream( inputPath );
		}

		if ( inputStream == null ) {
			ClassLoader loader = run( GetClassLoader.fromContext() );
			if ( loader != null ) {
				log.debug( "Trying to load " + path + " via TCCL" );
				inputStream = loader.getResourceAsStream( inputPath );
			}
		}

		if ( inputStream == null ) {
			log.debug( "Trying to load " + path + " via Hibernate Validator's class loader" );
			ClassLoader loader = ResourceLoaderHelper.class.getClassLoader();
			inputStream = loader.getResourceAsStream( inputPath );
		}

		if ( inputStream == null ) {
			return null;
		}
		else if ( inputStream.markSupported() ) {
			return inputStream;
		}
		else {
			return new BufferedInputStream( inputStream );
		}
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
