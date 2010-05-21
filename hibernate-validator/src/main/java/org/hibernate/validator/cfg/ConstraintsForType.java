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
import java.lang.reflect.Constructor;

import org.hibernate.validator.util.ReflectionHelper;

import static java.lang.annotation.ElementType.TYPE;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintsForType {
	private static final String EMPTY_PROPERTY = "";

	private final ConstraintMapping mapping;
	private final Class<?> beanClass;
	private String property;
	private ElementType elementType;

	public ConstraintsForType(Class<?> beanClass, ConstraintMapping mapping) {
		this( beanClass, EMPTY_PROPERTY, TYPE, mapping );
	}

	public ConstraintsForType(Class<?> beanClass, String property, ElementType type, ConstraintMapping mapping) {
		this.beanClass = beanClass;
		this.mapping = mapping;
		this.property = property;
		this.elementType = type;
	}

	public <A extends Annotation, T extends ConstraintDefinition<A>> T constraint(Class<T> definition) {
		final Constructor<T> constructor = ReflectionHelper.getConstructor(
				definition, Class.class, String.class, ElementType.class, ConstraintMapping.class
		);

		final T constraintDefinition = ReflectionHelper.newConstructorInstance(
				constructor, beanClass, property, elementType, mapping
		);
		mapping.addConstraintConfig( constraintDefinition );
		return constraintDefinition;
	}

	public ConstraintsForType property(String property, ElementType type) {
		return new ConstraintsForType( beanClass, property, type, mapping );
	}

	public ConstraintsForType valid(String property, ElementType type) {
		mapping.addCascadeConfig(new CascadeDefinition( beanClass, property, type));
		return this;
	}

	public ConstraintsForType type(Class<?> type) {
		return new ConstraintsForType( type, mapping );
	}
}


