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

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides utility methods for working with classes.
 * 
 * @author Mark Hobson
 * @version $Id: ClassUtils.java 82 2010-11-01 11:22:10Z markhobson $
 * @see Class
 */
final class ClassUtils
{
	// constants --------------------------------------------------------------
	
	private static final Map<String, String> PRIMITIVE_DESCRIPTORS_BY_CLASS_NAME =
		createPrimitiveDescriptorsByClassName();
	
	// constructors -----------------------------------------------------------
	
	private ClassUtils()
	{
		throw new AssertionError();
	}
	
	// public methods ---------------------------------------------------------
	
	public static String getUnqualifiedClassName(Class<?> klass)
	{
		return getUnqualifiedClassName(klass.getName());
	}

	public static String getUnqualifiedClassName(String className)
	{
		int dot = className.lastIndexOf('.');
		
		return (dot == -1) ? className : className.substring(dot + 1);
	}
	
	public static String getSimpleClassName(Class<?> klass)
	{
		return getSimpleClassName(klass.getName());
	}
	
	public static String getSimpleClassName(String className)
	{
		int index = className.lastIndexOf('$');
		
		if (index == -1)
		{
			index = className.lastIndexOf('.');
		}
		
		return (index == -1) ? className : className.substring(index + 1);
	}
	
	public static Class<?> getArrayType(Class<?> componentType)
	{
		return Array.newInstance(componentType, 0).getClass();
	}
	
	public static Class<?> valueOf(String className)
	{
		if (isPrimitiveClassName(className))
		{
			return valueOfPrimitive(className);
		}
		
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		try
		{
			return Class.forName(className, true, classLoader);
		}
		catch (ClassNotFoundException exception)
		{
			return null;
		}
	}
	
	// private methods --------------------------------------------------------
	
	private static Map<String, String> createPrimitiveDescriptorsByClassName()
	{
		Map<String, String> primitiveDescriptorsByClassName = new HashMap<String, String>();
		
		// from JVM specification 4.3.2
		// see http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#84645
		primitiveDescriptorsByClassName.put("byte", "B");
		primitiveDescriptorsByClassName.put("char", "C");
		primitiveDescriptorsByClassName.put("double", "D");
		primitiveDescriptorsByClassName.put("float", "F");
		primitiveDescriptorsByClassName.put("int", "I");
		primitiveDescriptorsByClassName.put("long", "J");
		primitiveDescriptorsByClassName.put("short", "S");
		primitiveDescriptorsByClassName.put("boolean", "Z");
		
		return Collections.unmodifiableMap(primitiveDescriptorsByClassName);
	}
	
	private static Class<?> valueOfPrimitive(String className)
	{
		// cannot load primitives directly so load primitive array type and use component type instead
		
		String descriptor = getPrimitiveDescriptor(className);
		String arrayDescriptor = getArrayDescriptor(descriptor);
		Class<?> arrayType = valueOf(arrayDescriptor);
		
		return arrayType.getComponentType();
	}
	
	private static boolean isPrimitiveClassName(String className)
	{
		return PRIMITIVE_DESCRIPTORS_BY_CLASS_NAME.containsKey(className);
	}
	
	private static String getPrimitiveDescriptor(String className)
	{
		if (!isPrimitiveClassName(className))
		{
			throw new IllegalArgumentException("className is not a primitive class name: " + className);
		}
		
		return PRIMITIVE_DESCRIPTORS_BY_CLASS_NAME.get(className);
	}
	
	private static String getArrayDescriptor(String componentDescriptor)
	{
		return "[" + componentDescriptor;
	}
}
