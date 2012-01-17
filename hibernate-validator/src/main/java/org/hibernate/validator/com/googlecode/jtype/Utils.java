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

/**
 * Various internal utility methods.
 * 
 * @author Mark Hobson
 * @version $Id: Utils.java 2 2009-02-02 22:28:39Z markhobson $
 */
final class Utils
{
	// constructors -----------------------------------------------------------
	
	private Utils()
	{
		throw new AssertionError();
	}
	
	// public methods ---------------------------------------------------------
	
	public static <T> T checkNotNull(T object, String name)
	{
		if (object == null)
		{
			throw new NullPointerException(name + " cannot be null");
		}
		
		return object;
	}
	
	public static void checkTrue(boolean condition, String message, Object value)
	{
		if (!condition)
		{
			throw new IllegalArgumentException(message + value);
		}
	}
	
	public static int nullHashCode(Object object)
	{
		return (object != null) ? object.hashCode() : 0;
	}
	
	public static boolean nullEquals(Object object1, Object object2)
	{
		return (object1 == null) ? (object2 == null) : object1.equals(object2);
	}
}
