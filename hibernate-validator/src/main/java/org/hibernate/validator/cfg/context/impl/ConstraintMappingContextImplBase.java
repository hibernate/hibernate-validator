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
package org.hibernate.validator.cfg.context.impl;

import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import javax.validation.ValidationException;

import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.util.Contracts;
import org.hibernate.validator.util.ReflectionHelper;

/**
 * Base class for implementations of constraint mapping creational context types.
 *
 * @author Gunnar Morling
 */
public abstract class ConstraintMappingContextImplBase {

	protected final Class<?> beanClass;

	protected final ConstraintMappingContext mapping;

	public ConstraintMappingContextImplBase(Class<?> beanClass, ConstraintMappingContext mapping) {

		this.beanClass = beanClass;
		this.mapping = mapping;
	}

	public <C> TypeConstraintMappingContext<C> type(Class<C> type) {

		Contracts.assertNotNull( beanClass, "The bean type must not be null when creating a constraint mapping." );

		return new TypeConstraintMappingContextImpl<C>( type, mapping );
	}

	public PropertyConstraintMappingContext property(String property, ElementType elementType) {

		Contracts.assertNotNull( property, "The property name must not be null." );
		Contracts.assertNotNull( elementType, "The element type must not be null." );

		if ( property.length() == 0 ) {
			throw new IllegalArgumentException( "The property name must not be empty." );
		}

		Member member = ReflectionHelper.getMember(
				beanClass, property, elementType
		);

		if ( member == null ) {
			throw new ValidationException(
					"The class " + beanClass + " does not have a property '"
							+ property + "' with access " + elementType
			);
		}

		return new PropertyConstraintMappingContextImpl( beanClass, member, mapping );
	}

	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {

		Contracts.assertNotNull( name, "The method name must not be null." );

		Method method = ReflectionHelper.getDeclaredMethod( beanClass, name, parameterTypes );

		if ( method == null ) {
			StringBuilder sb = new StringBuilder();
			for ( Class<?> oneParameterType : parameterTypes ) {
				sb.append( oneParameterType.getName() + ", " );
			}

			String parameterTypesAsString = sb.length() > 2 ? sb.substring( 0, sb.length() - 2 ) : sb.toString();

			throw new IllegalArgumentException(
					String.format( "Type %s doesn't have a method %s(%s).", beanClass, name, parameterTypesAsString )
			);
		}

		return new MethodConstraintMappingContextImpl( beanClass, method, mapping );
	}

}