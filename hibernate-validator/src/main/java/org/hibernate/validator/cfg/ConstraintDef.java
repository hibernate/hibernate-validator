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
import java.util.HashMap;
import java.util.Map;
import javax.validation.Payload;
import javax.validation.ValidationException;

import org.hibernate.validator.util.ReflectionHelper;

/**
 * Base class for all constraint definition types. Each sub type represents a
 * single constraint annotation type and allows to add this constraint to a bean
 * class in a programmatic type-safe way with help of the
 * {@link ConstraintMapping} API.
 *
 * @param <C> The type of a concrete sub type. Following to the
 * "self referencing generic type" pattern each sub type has to be
 * parametrized with itself.
 * @param <A> The constraint annotation type represented by a concrete sub type.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public abstract class ConstraintDef<C extends ConstraintDef<C, A>, A extends Annotation> {

	// Note on visibility of members: In order to avoid the need for public
	// getter methods for the fields of this class (which would pollute it's
	// fluent API) they are protected instead of private. To get access to
	// these fields outside of this class a ConstraintDefAccessor instance can
	// be created from it, which offers public getter methods.

	/**
	 * The constraint annotation type of this definition.
	 */
	protected final Class<A> constraintType;

	/**
	 * A map with the annotation parameters of this definition. Keys are
	 * property names of this definition's annotation type, values are
	 * annotation parameter values of the appropriate types.
	 */
	protected final Map<String, Object> parameters;

	/**
	 * The bean type of this definition.
	 */
	protected final Class<?> beanType;

	/**
	 * The element type of this definition.
	 */
	protected final ElementType elementType;

	/**
	 * The property name of this definition, if it represents a property level constraint.
	 */
	protected final String property;

	/**
	 * The constraint mapping owning this constraint definition.
	 */
	protected final ConstraintMapping mapping;

	public ConstraintDef(Class<?> beanType, Class<A> constraintType, String property, ElementType elementType, ConstraintMapping mapping) {
		this( beanType, constraintType, property, elementType, new HashMap<String, Object>(), mapping );
	}

	protected ConstraintDef(Class<?> beanType, Class<A> constraintType, String property, ElementType elementType, Map<String, Object> parameters, ConstraintMapping mapping) {
		if ( beanType == null ) {
			throw new ValidationException( "Null is not a valid bean type" );
		}

		if ( mapping == null ) {
			throw new ValidationException( "ConstraintMapping cannot be null" );
		}

		if ( ElementType.FIELD.equals( elementType ) || ElementType.METHOD.equals( elementType ) ) {
			if ( property == null || property.length() == 0 ) {
				throw new ValidationException( "A property level constraint cannot have a null or empty property name" );
			}

			if ( !ReflectionHelper.propertyExists( beanType, property, elementType ) ) {
				throw new ValidationException(
						"The class " + beanType + " does not have a property '"
								+ property + "' with access " + elementType
				);
			}
		}

		this.beanType = beanType;
		this.constraintType = constraintType;
		this.parameters = parameters;
		this.property = property;
		this.elementType = elementType;
		this.mapping = mapping;
	}

	@SuppressWarnings("unchecked")
	private C getThis() {
		return (C) this;
	}

	protected C addParameter(String key, Object value) {
		parameters.put( key, value );
		return getThis();
	}

	public C message(String message) {
		addParameter( "message", message );
		return getThis();
	}

	public C groups(Class<?>... groups) {
		addParameter( "groups", groups );
		return getThis();
	}

	public C payload(Class<? extends Payload>... payload) {
		addParameter( "payload", payload );
		return getThis();
	}

	public <B extends Annotation, D extends ConstraintDef<D, B>> D constraint(Class<D> definition) {
		final Constructor<D> constructor = ReflectionHelper.getConstructor(
				definition, Class.class, String.class, ElementType.class, ConstraintMapping.class
		);

		final D constraintDefinition = ReflectionHelper.newConstructorInstance(
				constructor, beanType, property, elementType, mapping
		);

		mapping.addConstraintConfig( constraintDefinition );
		return constraintDefinition;
	}

	public <A extends Annotation> GenericConstraintDef<A> genericConstraint(Class<A> definition) {

		GenericConstraintDef<A> constraintDefinition = new GenericConstraintDef<A>(
				beanType, definition, property, elementType, mapping
		);

		mapping.addConstraintConfig( constraintDefinition );
		return constraintDefinition;
	}

	public ConstraintsForType property(String property, ElementType type) {
		return new ConstraintsForType( beanType, property, type, mapping );
	}

	public ConstraintsForType type(Class<?> type) {
		return new ConstraintsForType( type, mapping );
	}

	public ConstraintsForType valid(String property, ElementType type) {
		mapping.addCascadeConfig( new CascadeDef( beanType, property, type ) );
		return new ConstraintsForType( beanType, mapping );
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( this.getClass().getName() );
		sb.append( "{beanType=" ).append( beanType );
		sb.append( ", constraintType=" ).append( constraintType );
		sb.append( ", parameters=" ).append( parameters );
		sb.append( ", elementType=" ).append( elementType );
		sb.append( ", property='" ).append( property ).append( '\'' );
		sb.append( '}' );
		return sb.toString();
	}

}
