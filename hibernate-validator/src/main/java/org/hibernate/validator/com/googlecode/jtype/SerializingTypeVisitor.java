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
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * 
 * 
 * @author Mark Hobson
 * @version $Id: SerializingTypeVisitor.java 84 2010-11-04 10:48:08Z markhobson $
 */
class SerializingTypeVisitor extends AbstractTypeVisitor
{
	// fields -----------------------------------------------------------------
	
	private final ClassSerializer serializer;
	
	private final StringBuilder builder;
	
	// constructors -----------------------------------------------------------
	
	public SerializingTypeVisitor(ClassSerializer serializer)
	{
		Utils.checkNotNull(serializer, "serializer");
		
		this.serializer = serializer;
		
		builder = new StringBuilder();
	}

	// TypeVisitor methods ----------------------------------------------------
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(Class<?> type)
	{
		if (type.isArray())
		{
			visit(type.getComponentType());
			
			builder.append("[]");
		}
		else
		{
			builder.append(serializer.toString(type));
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public <D extends GenericDeclaration> boolean beginVisit(TypeVariable<D> type)
	{
		builder.append(type.getName());
		
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitTypeVariableBound(Type bound, int index)
	{
		if (!(bound == Object.class && index == 0))
		{
			builder.append((index == 0) ? " extends " : " & ");
		
			visit(bound);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visit(GenericArrayType type)
	{
		visit(type.getGenericComponentType());
		
		builder.append("[]");
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean beginVisit(ParameterizedType type)
	{
		Type ownerType = type.getOwnerType();
		
		if (ownerType != null)
		{
			visit(ownerType);
			
			builder.append(".");
		}
		
		visit(type.getRawType());
		
		if (type.getActualTypeArguments().length > 0)
		{
			builder.append("<");
		}
		
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitActualTypeArgument(Type type, int index)
	{
		if (index > 0)
		{
			builder.append(", ");
		}
		
		visit(type);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void endVisit(ParameterizedType type)
	{
		if (type.getActualTypeArguments().length > 0)
		{
			builder.append(">");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean beginVisit(WildcardType type)
	{
		builder.append("?");
		
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitUpperBound(Type bound, int index)
	{
		if (!(bound == Object.class && index == 0))
		{
			builder.append((index == 0) ? " extends " : " & ");
		
			visit(bound);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitLowerBound(Type bound, int index)
	{
		builder.append((index == 0) ? " super " : " & ");
		
		visit(bound);
	}
	
	// Object methods ---------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return builder.toString();
	}
}
