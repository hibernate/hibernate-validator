/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.xml;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * HV-1025 - On some JVMs (eg the IBM JVM) the JAXB implementation closes the underlying input stream.
 * <p>
 * To prevent this we wrap the input stream to be able to ignore the close event. It is the responsibility of the client
 * API to close the stream (as per Bean Validation spec, see jakarta.validation.Configuration).
 *
 * @author Guillaume Smet
 */
public class CloseIgnoringInputStream extends FilterInputStream {

	public CloseIgnoringInputStream(InputStream in) {
		super( in );
	}

	@Override
	public void close() {
		// do nothing
	}
}
