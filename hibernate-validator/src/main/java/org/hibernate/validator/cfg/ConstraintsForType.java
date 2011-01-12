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
import java.util.Arrays;

import org.hibernate.validator.util.ReflectionHelper;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Via instances of this class constraints and cascading properties can be configured for a single bean class.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public final class ConstraintsForType {
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

	/**
	 * Adds a new constraint.
	 *
	 * @param definition The constraint definition class
	 *
	 * @return A constraint definition class allowing to specify additional constraint parameters.
	 */
	public <A extends Annotation, T extends ConstraintDef<T, A>> T constraint(Class<T> definition) {
		final Constructor<T> constructor = ReflectionHelper.getConstructor(
				definition, Class.class, String.class, ElementType.class, ConstraintMapping.class
		);

		final T constraintDefinition = ReflectionHelper.newConstructorInstance(
				constructor, beanClass, property, elementType, mapping
		);
		mapping.addConstraintConfig( constraintDefinition );
		return constraintDefinition;
	}

	/**
	 * Adds a new constraint in a generic way. The attributes of the constraint can later on be
	 * set by invoking {@link GenericConstraintDef#addParameter(String, Object)}.
	 *
	 * @param <A> The annotation type of the constraint to add
	 * @param definition The constraint to add
	 *
	 * @return A generic constraint definition class allowing to specify additional constraint parameters
	 */
	public <A extends Annotation> GenericConstraintDef<A> genericConstraint(Class<A> definition) {

		GenericConstraintDef<A> constraintDefinition = new GenericConstraintDef<A>(
				beanClass, definition, property, elementType, mapping
		);

		mapping.addConstraintConfig( constraintDefinition );
		return constraintDefinition;
	}

	/**
	 * Changes the property for which added constraints apply. Until this method is called constraints apply on
	 * class level. After calling this method constraints apply on the specified property with the given access type.
	 *
	 * @param property The property on which to apply the following constraints (Java Bean notation)
	 * @param type The access type (field/property)
	 *
	 * @return Returns itself for method chaining
	 */
	public ConstraintsForType property(String property, ElementType type) {
		return new ConstraintsForType( beanClass, property, type, mapping );
	}

	public ConstraintsForType valid(String property, ElementType type) {
		mapping.addCascadeConfig( new CascadeDef( beanClass, property, type ) );
		return this;
	}

	/**
	 * Defines the default groups sequence for the bean class of this instance.
	 *
	 * @param defaultGroupSequence the default group sequence.
	 *
	 * @return Returns itself for method chaining.
	 */
	public ConstraintsForType defaultGroupSequence(Class<?>... defaultGroupSequence) {
		mapping.addDefaultGroupSequence( beanClass, Arrays.asList( defaultGroupSequence ) );
		return this;
	}

	/**
	 * Creates a new {@code ConstraintsForType} in order to define constraints on a new bean type.
	 *
	 * @param type the bean type
	 *
	 * @return a new {@code ConstraintsForType} instance
	 */
	public ConstraintsForType type(Class<?> type) {
		return new ConstraintsForType( type, mapping );
	}
}


