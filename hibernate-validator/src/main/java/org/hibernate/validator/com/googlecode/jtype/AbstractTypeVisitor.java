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
 * @version $Id: AbstractTypeVisitor.java 73 2010-03-24 21:47:29Z markhobson $
 */
abstract class AbstractTypeVisitor implements TypeVisitor
{
	// TypeVisitor methods ----------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	public void visit(Class<?> type)
	{
		// no-op
	}
	
	/**
	 * {@inheritDoc}
	 */
	public <D extends GenericDeclaration> boolean beginVisit(TypeVariable<D> type)
	{
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void visitTypeVariableBound(Type bound, int index)
	{
		visit(bound);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public <D extends GenericDeclaration> void endVisit(TypeVariable<D> type)
	{
		// no-op
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void visit(GenericArrayType type)
	{
		visit(type.getGenericComponentType());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean beginVisit(ParameterizedType type)
	{
		visit(type.getRawType());
		
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void visitActualTypeArgument(Type type, int index)
	{
		visit(type);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void endVisit(ParameterizedType type)
	{
		// no-op
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean beginVisit(WildcardType type)
	{
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void visitUpperBound(Type bound, int index)
	{
		visit(bound);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void visitLowerBound(Type bound, int index)
	{
		visit(bound);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void endVisit(WildcardType type)
	{
		// no-op
	}
	
	// protected methods ------------------------------------------------------
	
	protected void visit(Type type)
	{
		TypeUtils.accept(type, this);
	}
}
