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

import java.lang.annotation.ElementType;
import javax.validation.ValidationException;

import org.hibernate.validator.util.ReflectionHelper;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static org.hibernate.validator.util.ReflectionHelper.propertyExists;

/**
 * @author Hardy Ferentschik
 */
public class CascadeDef {
	private final Class<?> beanType;
	private final ElementType elementType;
	private final String property;

	public CascadeDef(Class<?> beanType, String property, ElementType elementType) {
		if ( beanType == null ) {
			throw new ValidationException( "Null is not a valid bean type" );
		}

		if ( FIELD.equals( elementType ) || METHOD.equals( elementType ) ) {
			if ( property == null || property.length() == 0 ) {
				throw new ValidationException( "A valid property name has to be specified" );
			}

			if ( !propertyExists( beanType, property, elementType ) ) {
				throw new ValidationException(
						"The " + beanType + " does not have a property '"
								+ property + "' with access " + elementType
				);
			}
		}

		this.beanType = beanType;
		this.property = property;
		this.elementType = elementType;
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

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "CascadeDefinition" );
		sb.append( "{beanType=" ).append( beanType );
		sb.append( ", elementType=" ).append( elementType );
		sb.append( ", property='" ).append( property ).append( '\'' );
		sb.append( '}' );
		return sb.toString();
	}
}
