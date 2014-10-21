/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedExceptionAction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Returns a new {@link JAXBContext} for the given class.
 *
 * @author Gunnar Morling
 */
public final class NewJaxbContext implements PrivilegedExceptionAction<JAXBContext> {

	private final Class<?> clazz;

	public static NewJaxbContext action(Class<?> clazz) {
		return new NewJaxbContext( clazz );
	}

	private NewJaxbContext(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public JAXBContext run() throws JAXBException {
		return JAXBContext.newInstance( clazz );
	}
}
