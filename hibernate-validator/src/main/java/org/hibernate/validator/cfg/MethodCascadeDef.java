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

import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import javax.validation.ValidationException;

import static java.lang.annotation.ElementType.PARAMETER;
import static org.hibernate.validator.util.ReflectionHelper.getDeclaredMethod;

/**
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class MethodCascadeDef {
	private final Class<?> beanType;
	private final Method method;
	private final ElementType elementType;
	private final int index;

	public MethodCascadeDef(Class<?> beanType, String methodName, Class<?>[] parameterTypes, int index, ElementType elementType) {
		if ( beanType == null ) {
			throw new ValidationException( "Null is not a valid bean type" );
		}

		if ( methodName == null || methodName.length() == 0 ) {
			throw new ValidationException( "A valid method name has to be specified" );
		}

		if ( PARAMETER.equals( elementType ) && ( index < 0 || index >= parameterTypes.length ) ) {
			throw new ValidationException( "A valid parameter index has to be specified for method " + methodName );
		}

		Method clazzMethod = getDeclaredMethod( beanType, methodName, parameterTypes );
		if ( clazzMethod == null || Modifier.isStatic( clazzMethod.getModifiers() ) || clazzMethod.isSynthetic() ) {
			throw new ValidationException(
					"The class " + beanType + " doesn't have a method named "
							+ methodName + " with parameter types " + Arrays.toString( parameterTypes )
			);
		}

		this.beanType = beanType;
		this.method = clazzMethod;
		this.elementType = elementType;
		this.index = index;
	}

	public Class<?> getBeanType() {
		return beanType;
	}

	public ElementType getElementType() {
		return elementType;
	}

	public Method getMethod() {
		return method;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append( "CascadeDefinition" );
		sb.append( "{beanType=" ).append( beanType );
		sb.append( ", elementType=" ).append( elementType );
		sb.append( ", method='" ).append( method ).append( '\'' );
		sb.append( ", index=" ).append( index );
		sb.append( '}' );
		return sb.toString();
	}
}
