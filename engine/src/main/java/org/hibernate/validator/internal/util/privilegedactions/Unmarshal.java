/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import java.security.PrivilegedExceptionAction;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;

/**
 * Unmarshals the given source.
 *
 * @author Gunnar Morling
 */
public final class Unmarshal<T> implements PrivilegedExceptionAction<JAXBElement<T>> {

	private final Unmarshaller unmarshaller;
	private final Source source;
	private final Class<T> clazz;

	public static <T> Unmarshal<T> action(Unmarshaller unmarshaller, Source source, Class<T> clazz) {
		return new Unmarshal<T>( unmarshaller, source, clazz );
	}

	private Unmarshal(Unmarshaller unmarshaller, Source source, Class<T> clazz) {
		this.unmarshaller = unmarshaller;
		this.source = source;
		this.clazz = clazz;
	}

	@Override
	public JAXBElement<T> run() throws JAXBException {
		return unmarshaller.unmarshal( source, clazz );
	}
}
