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
 * Factory for creating {@code ClassSerializer}s.
 * 
 * @author Mark Hobson
 * @version $Id: ClassSerializers.java 38 2009-08-26 11:21:03Z markhobson $
 * @see ClassSerializer
 */
public final class ClassSerializers
{
	// constants --------------------------------------------------------------
	
	public static final ClassSerializer QUALIFIED = new ClassSerializer()
	{
		public String toString(Class<?> klass)
		{
			return klass.getName();
		}
	};
	
	public static final ClassSerializer UNQUALIFIED = new ClassSerializer()
	{
		public String toString(Class<?> klass)
		{
			return ClassUtils.getUnqualifiedClassName(klass);
		}
	};
	
	public static final ClassSerializer SIMPLE = new ClassSerializer()
	{
		public String toString(Class<?> klass)
		{
			return ClassUtils.getSimpleClassName(klass);
		}
	};
	
	// constructors -----------------------------------------------------------
	
	private ClassSerializers()
	{
		throw new AssertionError();
	}
}
