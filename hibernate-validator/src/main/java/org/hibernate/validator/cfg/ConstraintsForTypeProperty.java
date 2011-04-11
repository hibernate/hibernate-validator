/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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

/**
 * Via instances of this class properties constraints can be configured for a single bean class.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class ConstraintsForTypeProperty {
	private final ConstraintMapping mapping;
	private final Class<?> beanClass;
	private final String property;
	private final ElementType elementType;

	public ConstraintsForTypeProperty(Class<?> beanClass, String property, ElementType elementType, ConstraintMapping mapping) {
		this.beanClass = beanClass;
		this.mapping = mapping;
		this.property = property;
		this.elementType = elementType;
	}

	/**
	 * Adds a new constraint.
	 *
	 * @param definition The constraint definition class.
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
	 * Adds a new constraint in a generic way.
	 * <p>
	 * The attributes of the constraint can later on be set by invoking
	 * {@link GenericConstraintDef#addParameter(String, Object)}.
	 * </p>
	 *
	 * @param <A> The annotation type of the constraint to add.
	 * @param definition The constraint to add.
	 *
	 * @return A generic constraint definition class allowing to specify additional constraint parameters.
	 */
	public <A extends Annotation> GenericConstraintDef<A> genericConstraint(Class<A> definition) {
		final GenericConstraintDef<A> constraintDefinition = new GenericConstraintDef<A>(
				beanClass, definition, property, elementType, mapping
		);

		mapping.addConstraintConfig( constraintDefinition );
		return constraintDefinition;
	}

	/**
	 * Marks the current property as cascadable.
	 *
	 * @return Returns itself for method chaining.
	 */
	public ConstraintsForTypeProperty valid() {
		mapping.addCascadeConfig( new CascadeDef( beanClass, property, elementType ) );
		return this;
	}

	/**
	 * Creates a new {@code ConstraintsForType} in order to define constraints on a new bean type.
	 *
	 * @param type The bean type.
	 *
	 * @return Returns a new {@code ConstraintsForType} instance.
	 */
	public ConstraintsForType type(Class<?> type) {
		return new ConstraintsForType( type, mapping );
	}

	/**
	 * Changes the property for which added constraints apply.
	 *
	 * @param property The property on which to apply the following constraints (Java Bean notation).
	 * @param type The access type (field/property).
	 *
	 * @return Returns itself for method chaining.
	 */
	public ConstraintsForTypeProperty property(String property, ElementType type) {
		return new ConstraintsForTypeProperty( beanClass, property, type, mapping );
	}

	/**
	 * Returns a new {@code ConstraintsForMethod} instance allowing to define
	 * constraints for the given method.
	 *
	 * @param name The method name.
	 * @param parameterTypes The method parameter types.
	 *
	 * @return Returns a new {@code ConstraintsForMethod} instance allowing method chaining.
	 */
	public ConstraintsForTypeMethod method(String name, Class<?>... parameterTypes) {
		return new ConstraintsForTypeMethod( beanClass, name, parameterTypes, mapping );
	}
}
