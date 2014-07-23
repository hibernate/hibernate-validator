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
package org.hibernate.validator.internal.cfg.context;

import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;
import org.hibernate.validator.internal.util.Contracts;
import org.hibernate.validator.internal.util.ReflectionHelper;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredField;
import org.hibernate.validator.internal.util.privilegedactions.GetDeclaredMethod;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;

import static org.hibernate.validator.internal.util.logging.Messages.MESSAGES;

/**
 * Base class for implementations of constraint mapping creational context types.
 *
 * @author Gunnar Morling
 */
public abstract class ConstraintMappingContextImplBase {

	private static final Log log = LoggerFactory.make();

	protected final Class<?> beanClass;
	protected final ConstraintMappingContext mapping;

	public ConstraintMappingContextImplBase(Class<?> beanClass, ConstraintMappingContext mapping) {
		this.beanClass = beanClass;
		this.mapping = mapping;
	}

	public <C> TypeConstraintMappingContext<C> type(Class<C> type) {
		Contracts.assertNotNull( beanClass, MESSAGES.beanTypeMustNotBeNull() );

		return new TypeConstraintMappingContextImpl<C>( type, mapping );
	}

	public PropertyConstraintMappingContext property(String property, ElementType elementType) {
		Contracts.assertNotNull( property, "The property name must not be null." );
		Contracts.assertNotNull( elementType, "The element type must not be null." );
		Contracts.assertNotEmpty( property, MESSAGES.propertyNameMustNotBeEmpty() );

		Member member = getMember( beanClass, property, elementType );

		if ( member == null ) {
			throw log.getUnableToFindPropertyWithAccessException( beanClass, property, elementType );
		}

		return new PropertyConstraintMappingContextImpl( beanClass, member, mapping );
	}

	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		Contracts.assertNotNull( name, MESSAGES.methodNameMustNotBeNull() );

		Method method = run( GetDeclaredMethod.action( beanClass, name, parameterTypes ) );

		if ( method == null ) {
			StringBuilder sb = new StringBuilder();
			for ( Class<?> oneParameterType : parameterTypes ) {
				sb.append( oneParameterType.getName() ).append( ", " );
			}

			String parameterTypesAsString = sb.length() > 2 ? sb.substring( 0, sb.length() - 2 ) : sb.toString();

			throw log.getUnableToFindMethodException( beanClass, name, parameterTypesAsString );
		}

		return new MethodConstraintMappingContextImpl( beanClass, method, mapping );
	}

	/**
	 * Returns the member with the given name and type.
	 *
	 * @param clazz The class from which to retrieve the member. Cannot be {@code null}.
	 * @param property The property name without 'is', 'get' or 'has'. Cannot be {@code null} or empty.
	 * @param elementType The element type. Either {@code ElementType.FIELD} or {@code ElementType METHOD}.
	 *
	 * @return the member which matching the name and type or {@code null} if no such member exists.
	 */
	public static Member getMember(Class<?> clazz, String property, ElementType elementType) {

		Contracts.assertNotNull( clazz, MESSAGES.classCannotBeNull() );

		if ( property == null || property.length() == 0 ) {
			throw log.getPropertyNameCannotBeNullOrEmptyException();
		}

		if ( !( ElementType.FIELD.equals( elementType ) || ElementType.METHOD.equals( elementType ) ) ) {
			throw log.getElementTypeHasToBeFieldOrMethodException();
		}

		Member member = null;
		if ( ElementType.FIELD.equals( elementType ) ) {
			GetDeclaredField action = GetDeclaredField.action( clazz, property );
			if ( System.getSecurityManager() != null ) {
				member = AccessController.doPrivileged( action );
			}
			else {
				member = action.run();
			}
		}
		else {
			String methodName = property.substring( 0, 1 ).toUpperCase() + property.substring( 1 );
			for ( String prefix : ReflectionHelper.PROPERTY_ACCESSOR_PREFIXES ) {
				GetMethod action = GetMethod.action( clazz, prefix + methodName );
				if ( System.getSecurityManager() != null ) {
					member = AccessController.doPrivileged( action );
				}
				else {
					member = action.run();
				}
				if ( member != null ) {
					break;
				}
			}
		}
		return member;
	}

	/**
	 * Runs the given privileged action, using a privileged block if required.
	 * <p>
	 * <b>NOTE:</b> This must never be changed into a publicly available method to avoid execution of arbitrary
	 * privileged actions within HV's protection domain.
	 */
	private static <T> T run(PrivilegedAction<T> action) {
		return System.getSecurityManager() != null ? AccessController.doPrivileged( action ) : action.run();
	}
}
