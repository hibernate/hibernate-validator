/*
 * Copyright 2009 IIZUKA Software Technologies Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.validator.com.googlecode.jtype;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Factory for creating types.
 * 
 * @author Mark Hobson
 * @version $Id: Types.java 90 2010-11-05 15:58:29Z markhobson $
 * @see Type
 */
public final class Types
{
	// constructors -----------------------------------------------------------
	
	private Types()
	{
		throw new AssertionError();
	}
	
	// public methods ---------------------------------------------------------
	
	/**
	 * Creates a generic array type for the specified component type.
	 * 
	 * @param componentType
	 *            the component type
	 * @return the generic array type
	 */
	public static GenericArrayType genericArrayType(Type componentType)
	{
		return new DefaultGenericArrayType(componentType);
	}
	
	/**
	 * Creates a parameterized type for the specified raw type and actual type arguments.
	 * 
	 * @param rawType
	 *            the raw type
	 * @param actualTypeArguments
	 *            the actual type arguments
	 * @return the parameterized type
	 * @throws MalformedParameterizedTypeException
	 *             if the number of actual type arguments differs from those defined on the raw type
	 */
	public static ParameterizedType parameterizedType(Class<?> rawType, Type... actualTypeArguments)
	{
		return new DefaultParameterizedType(null, rawType, actualTypeArguments);
	}
	
	// private methods --------------------------------------------------------
	
}
