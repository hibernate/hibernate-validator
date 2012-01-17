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

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * Default implementation of a generic array type.
 * 
 * @author Mark Hobson
 * @version $Id: DefaultGenericArrayType.java 93 2010-11-16 10:39:34Z markhobson $
 * @see GenericArrayType
 */
class DefaultGenericArrayType implements GenericArrayType, Serializable
{
	// constants --------------------------------------------------------------
	
	private static final long serialVersionUID = 1L;
	
	// fields -----------------------------------------------------------------
	
	/**
	 * The component type of this array.
	 * 
	 * @serial
	 */
	private final Type componentType;
	
	// constructors -----------------------------------------------------------
	
	public DefaultGenericArrayType(Type componentType)
	{
		this.componentType = Utils.checkNotNull(componentType, "componentType");
	}
	
	// GenericArrayType methods -----------------------------------------------
	
	/**
	 * {@inheritDoc}
	 */
	public Type getGenericComponentType()
	{
		return componentType;
	}
	
	// Object methods ---------------------------------------------------------
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		return componentType.hashCode();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object object)
	{
		if (!(object instanceof GenericArrayType))
		{
			return false;
		}
		
		GenericArrayType type = (GenericArrayType) object;
		
		return componentType.equals(type.getGenericComponentType());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return TypeUtils.toString(this);
	}
}
