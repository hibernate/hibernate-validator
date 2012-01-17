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

/**
 * Provides utility methods for working with classes.
 * 
 * @author Mark Hobson
 * @version $Id: ClassUtils.java 82 2010-11-01 11:22:10Z markhobson $
 * @see Class
 */
final class ClassUtils
{
	// constructors -----------------------------------------------------------
	
	private ClassUtils()
	{
		throw new AssertionError();
	}
	
	// public methods ---------------------------------------------------------
	
	public static Class<?> getArrayType(Class<?> componentType)
	{
		return Array.newInstance(componentType, 0).getClass();
	}
	
}
