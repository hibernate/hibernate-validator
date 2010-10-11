/*
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * An {@link Enumeration} implementation, that wraps an {@link Iterator}. Can
 * be used to integrate older APIs working with enumerations with iterators.
 *
 * @author Gunnar Morling
 * @param <T> The enumerated type.
 */
public class IteratorEnumeration<T> implements Enumeration<T> {

	private Iterator<T> source;

	/**
	 * Creates a new IterationEnumeration.
	 *
	 * @param source The source iterator. Must not be null.
	 */
	public IteratorEnumeration(Iterator<T> source) {

		if ( source == null ) {
			throw new IllegalArgumentException( "Source must not be null" );
		}

		this.source = source;
	}

	public boolean hasMoreElements() {
		return source.hasNext();
	}

	public T nextElement() {
		return source.next();
	}
}
