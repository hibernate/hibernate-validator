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
import java.util.HashMap;
import java.util.Map;
import javax.validation.Payload;
import javax.validation.ValidationException;

import org.hibernate.validator.util.ReflectionHelper;

/**
 * @author Hardy Ferentschik
 */
public abstract class ConstraintDef<A extends Annotation> {
	protected Class<A> constraintType;
	protected final Map<String, Object> parameters;
	protected final Class<?> beanType;
	protected final ElementType elementType;
	protected final String property;
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

	protected ConstraintDef addParameter(String key, Object value) {
		parameters.put( key, value );
		return this;
	}

	public ConstraintDef message(String message) {
		addParameter( "message", message );
		return this;
	}

	public ConstraintDef groups(Class<?>... groups) {
		addParameter( "groups", groups );
		return this;
	}

	public ConstraintDef payload(Class<? extends Payload>... payload) {
		addParameter( "payload", payload );
		return this;
	}

	public <A extends Annotation, T extends ConstraintDef<A>> T constraint(Class<T> definition) {
		final Constructor<T> constructor = ReflectionHelper.getConstructor(
				definition, Class.class, String.class, ElementType.class, ConstraintMapping.class
		);

		final T constraintDefinition = ReflectionHelper.newConstructorInstance(
				constructor, beanType, property, elementType, mapping
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
