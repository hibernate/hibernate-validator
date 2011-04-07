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

import org.hibernate.validator.group.DefaultGroupSequenceProvider;
import org.hibernate.validator.util.ReflectionHelper;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Via instances of this class constraints and cascading properties can be configured for a single bean class.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class ConstraintsForType {
	private static final String EMPTY_PROPERTY = "";

	private final ConstraintMapping mapping;
	private final Class<?> beanClass;

	public ConstraintsForType(Class<?> beanClass, ConstraintMapping mapping) {
		this.beanClass = beanClass;
		this.mapping = mapping;
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
				constructor, beanClass, EMPTY_PROPERTY, TYPE, mapping
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
				beanClass, definition, EMPTY_PROPERTY, TYPE, mapping
		);

		mapping.addConstraintConfig( constraintDefinition );
		return constraintDefinition;
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
	 * Defines the default group sequence provider for the bean class of this instance.
	 *
	 * @param defaultGroupSequenceProviderClass The default group sequence provider class.
	 *
	 * @return Returns itself for method chaining.
	 */
	public <T extends DefaultGroupSequenceProvider<?>> ConstraintsForType defaultGroupSequenceProvider(Class<T> defaultGroupSequenceProviderClass) {
		mapping.addDefaultGroupSequenceProvider( beanClass, defaultGroupSequenceProviderClass );
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
	 * <p>
	 * Until this method is called constraints apply on class level. After calling this method constraints
	 * apply on the specified property with the given access type.
	 * </p>
	 *
	 * @param property The property on which to apply the following constraints (Java Bean notation).
	 * @param type The access type (field/property).
	 *
	 * @return Returns itself for method chaining.
	 */
	public ConstraintsForProperty property(String property, ElementType type) {
		return new ConstraintsForProperty( beanClass, property, type, mapping );
	}

	public ConstraintsForMethod method(String method, Class<?>... parameterTypes) {
		return new ConstraintsForMethod( beanClass, method, mapping, parameterTypes );
	}
}


