// $Id$
/*
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
package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Map;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintDefAccessor<A extends Annotation> extends ConstraintDef<A> {

	public ConstraintDefAccessor(Class<?> beanType, Class<A> constraintType, String property, ElementType elementType, Map<String, Object> parameters, ConstraintMapping mapping) {
		super( beanType, constraintType, property, elementType, parameters, mapping );
	}

	public Class<A> getConstraintType() {
		return constraintType;
	}

	public Map<String, Object> getParameters() {
		return this.parameters;
	}

	public ElementType getElementType() {
		return elementType;
	}

	public Class<?> getBeanType() {
		return beanType;
	}

	public String getProperty() {
		return property;
	}
}