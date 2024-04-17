/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.xml.config;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.actions.GetClassLoader;

/**
 * Helper methods for loading resource files
 *
 * @author Hardy Ferentschik
 */
final class ResourceLoaderHelper {

	private static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	private ResourceLoaderHelper() {
		// Not allowed
	}

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
			LOG.debug( "Trying to load " + path + " via user class loader" );
			inputStream = externalClassLoader.getResourceAsStream( inputPath );
		}

		if ( inputStream == null ) {
			ClassLoader loader = GetClassLoader.fromContext();
			if ( loader != null ) {
				LOG.debug( "Trying to load " + path + " via TCCL" );
				inputStream = loader.getResourceAsStream( inputPath );
			}
		}

		if ( inputStream == null ) {
			LOG.debug( "Trying to load " + path + " via Hibernate Validator's class loader" );
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
}
