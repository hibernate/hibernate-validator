/*
* JBoss, Home of Professional Open Source
* Copyright 2012, Red Hat, Inc. and/or its affiliates, and individual contributors
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
	static InputStream getResettableInputStreamForPath(String path) {
		//TODO not sure if it's the right thing to removing '/'
		String inputPath = path;
		if ( inputPath.startsWith( "/" ) ) {
			inputPath = inputPath.substring( 1 );
		}

		boolean isContextCL = true;
		// try the context class loader first
		ClassLoader loader = run( GetClassLoader.fromContext() );

		if ( loader == null ) {
			log.debug( "No default context class loader, fall back to Bean Validation's loader" );
			loader = run( GetClassLoader.fromClass( ResourceLoaderHelper.class ) );
			isContextCL = false;
		}
		InputStream inputStream = loader.getResourceAsStream( inputPath );

		// try the current class loader
		if ( isContextCL && inputStream == null ) {
			loader = run( GetClassLoader.fromClass( ResourceLoaderHelper.class ) );
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
