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
import java.security.AccessController;
import java.util.HashMap;
import java.util.Map;
import javax.validation.ValidationException;

import org.hibernate.validator.util.privilegedactions.NewInstance;
import org.hibernate.validator.util.ReflectionHelper;

/**
 * @author Hardy Ferentschik
 */
public class ConstraintDefinition<A extends Annotation> {
	private final Class<A> constraintType;
	private final Map<String, Object> parameters;
	private final Class<?> beanType;
	private final ElementType elementType;
	private final String property;
	private final ConstraintMapping mapping;

	public ConstraintDefinition(Class<?> beanType, Class<A> constraintType, String property, ElementType elementType, ConstraintMapping mapping) {
		if ( beanType == null ) {
			throw new ValidationException( "Null is not a valid bean type" );
		}

		if ( constraintType == null ) {
			throw new ValidationException( "Null is not a valid constraint type" );
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
		this.parameters = new HashMap<String, Object>();
		this.property = property;
		this.elementType = elementType;
		this.mapping = mapping;
	}

	protected ConstraintDefinition addParameter(String key, Object value) {
		parameters.put( key, value );
		return this;
	}

	public <A extends Annotation, T extends ConstraintDefinition<A>> T constraint(Class<T> definition) {
		T constraintDefinition = createConstraintDefinition( definition );
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
		return null;
	}

	public Class<A> getConstraintType() {
		return constraintType;
	}

	public Map<String, Object> getParameters() {
		return parameters;
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

	private <A extends Annotation, T extends ConstraintDefinition<A>> T createConstraintDefinition(Class<T> definition) {
		T constraintDefinition;
		NewInstance<T> newInstance = NewInstance.action( definition, "", beanType, property, elementType, mapping );

		if ( System.getSecurityManager() != null ) {
			constraintDefinition = AccessController.doPrivileged( newInstance );
		}
		else {
			constraintDefinition = newInstance.run();
		}
		return constraintDefinition;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "ConstraintDefinition" );
		sb.append( "{beanType=" ).append( beanType );
		sb.append( ", constraintType=" ).append( constraintType );
		sb.append( ", parameters=" ).append( parameters );
		sb.append( ", elementType=" ).append( elementType );
		sb.append( ", property='" ).append( property ).append( '\'' );
		sb.append( '}' );
		return sb.toString();
	}
}
