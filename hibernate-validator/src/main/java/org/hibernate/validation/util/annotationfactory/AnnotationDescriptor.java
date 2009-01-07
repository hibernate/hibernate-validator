// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.util.annotationfactory;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.HashMap;

/**
 * Encapsulates the data you need to create an annotation. In
 * particular, it stores the type of an <code>Annotation</code> instance
 * and the values of its elements.
 * The "elements" we're talking about are the annotation attributes,
 * not its targets (the term "element" is used ambiguously
 * in Java's annotations documentation).
 *
 * @author Paolo Perrotta
 * @author Davide Marchignoli
 */
public class AnnotationDescriptor {

	private final Class<? extends Annotation> type;

	private final Map<String, Object> elements = new HashMap<String, Object>();

	public AnnotationDescriptor(Class<? extends Annotation> annotationType) {
		type = annotationType;
	}

	public void setValue(String elementName, Object value) {
		elements.put( elementName, value );
	}

	public Object valueOf(String elementName) {
		return elements.get( elementName );
	}

	public boolean containsElement(String elementName) {
		return elements.containsKey( elementName );
	}

	public int numberOfElements() {
		return elements.size();
	}

	public Class<? extends Annotation> type() {
		return type;
	}
}
