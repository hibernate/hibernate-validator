/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.util.privilegedactions;

import java.security.PrivilegedExceptionAction;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLEventReader;

/**
 * Unmarshals the given source.
 *
 * @author Gunnar Morling
 */
public final class Unmarshal<T> implements PrivilegedExceptionAction<JAXBElement<T>> {

	private final Unmarshaller unmarshaller;
	private final XMLEventReader xmlEventReader;
	private final Class<T> clazz;

	public static <T> Unmarshal<T> action(Unmarshaller unmarshaller, XMLEventReader xmlEventReader, Class<T> clazz) {
		return new Unmarshal<T>( unmarshaller, xmlEventReader, clazz );
	}

	private Unmarshal(Unmarshaller unmarshaller, XMLEventReader xmlEventReader, Class<T> clazz) {
		this.unmarshaller = unmarshaller;
		this.xmlEventReader = xmlEventReader;
		this.clazz = clazz;
	}

	@Override
	public JAXBElement<T> run() throws JAXBException {
		return unmarshaller.unmarshal( xmlEventReader, clazz );
	}
}
