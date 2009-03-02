// $Id$// $Id$
/*
* JBoss, Home of Professional Open Source
* Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validation.engine;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintDescriptor;
import javax.validation.ValidationException;

import org.hibernate.validation.util.ReflectionHelper;

/**
 * Instances of this class abstract the constraint type  (class, method or field constraint) and gives access to
 * meta data about the constraint. This allows a unified handling of constraints in the validator imlpementation.
 *
 * @author Hardy Ferentschik
 */
public class MetaConstraint<T, A extends Annotation> {

	/**
	 * The constraint tree created from the constraint annotation.
	 */
	private final ConstraintTree<A> constraintTree;

	/**
	 * The type (class) the constraint was defined on. <code>null</code> if the constraint was specified on method or
	 * field level.
	 */
	private final Type type;

	/**
	 * The method the constraint was defined on. <code>null</code> if the constraint was specified on class or
	 * field level.
	 */
	private final Method method;

	/**
	 * The field the constraint was defined on. <code>null</code> if the constraint was specified on class or
	 * method level.
	 */
	private final Field field;

	/**
	 * The JavaBeans name for this constraint.
	 */
	private final String propertyName;

	/**
	 * Describes on which level (<code>TYPE</code>, <code>METHOD</code>, <code>FIELD</code>) the constraint was
	 * defined on.
	 */
	private final ElementType elementType;

	/**
	 * The class of the bean hosting this constraint.
	 */
	private final Class<T> beanClass;

	public MetaConstraint(Type t, ConstraintDescriptor<A> constraintDescriptor) {
		this( t, null, null, ElementType.TYPE, ( Class<T> ) t.getClass(), constraintDescriptor, "" );
	}

	public MetaConstraint(Method m, Class<T> beanClass, ConstraintDescriptor<A> constraintDescriptor) {
		this(
				null,
				m,
				null,
				ElementType.METHOD,
				beanClass,
				constraintDescriptor,
				ReflectionHelper.getPropertyName( m )
		);
	}

	public MetaConstraint(Field f, Class<T> beanClass, ConstraintDescriptor<A> constraintDescriptor) {
		this( null, null, f, ElementType.FIELD, beanClass, constraintDescriptor, f.getName() );
	}

	private MetaConstraint(Type t, Method m, Field f, ElementType elementType, Class<T> beanClass, ConstraintDescriptor<A> constraintDescriptor, String property) {
		this.type = t;
		this.method = m;
		this.field = f;
		this.elementType = elementType;
		this.propertyName = property;
		this.beanClass = beanClass;
		constraintTree = new ConstraintTree<A>( constraintDescriptor );
	}


	/**
	 * @return Returns the list of groups this constraint is part of. This might include the default group even when
	 *         it is not explicitly specified, but part of the redefined default group list of the hosting bean.
	 */
	public Set<Class<?>> getGroupList() {
		return constraintTree.getDescriptor().getGroups();
	}

	/**
	 * @param o the object from which to retrieve the value.
	 *
	 * @return Returns the value for this constraint from the specified object. Depending on the type either the value itself
	 *         is returned of method or field access is used to access the value.
	 */
	public Object getValue(Object o) {
		switch ( elementType ) {
			case TYPE: {
				return o;
			}
			case METHOD: {
				return ReflectionHelper.getValue( method, o );
			}
			case FIELD: {
				return ReflectionHelper.getValue( field, o );
			}
			default: {
				throw new ValidationException(
						"Invalid state of MetaConstraint. Parameter elementType has unexpected value - " + elementType
				);
			}
		}
	}

	/**
	 * @return Returns <code>true</code> in case the constraint is defined on a collection, <code>false</code>
	 *         otherwise.
	 */
	public boolean isCollection() {
		Type t = typeOfAnnoatedElement();
		return ReflectionHelper.isCollection( t );
	}

	/**
	 * @return Returns <code>true</code> in case the constraint is defined on an array, <code>false</code>
	 *         otherwise.
	 */
	public boolean isArray() {
		Type t = typeOfAnnoatedElement();
		return ReflectionHelper.isArray( t );
	}

	public ConstraintDescriptor getDescriptor() {
		return constraintTree.getDescriptor();
	}

	public Method getMethod() {
		return method;
	}

	public Field getField() {
		return field;
	}

	public Type getType() {
		return type;
	}

	public Class<T> getBeanClass() {
		return beanClass;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public ElementType getElementType() {
		return elementType;
	}

	public ConstraintTree getConstraintTree() {
		return constraintTree;
	}

	public <T> boolean validateConstraint(ExecutionContext<T> executionContext) {
		final Object leafBeanInstance = executionContext.peekValidatedBean();
		Object value = getValue( leafBeanInstance );
		List<ConstraintViolationImpl<T>> constraintViolations = new ArrayList<ConstraintViolationImpl<T>>();
		constraintTree.validateConstraints( value, executionContext, constraintViolations );
		if ( constraintViolations.size() > 0 ) {
			executionContext.addConstraintFailures( constraintViolations );
			return false;
		}
		return true;
	}

	public <T> boolean validateConstraint(Object value, ExecutionContext<T> executionContext) {
		List<ConstraintViolationImpl<T>> constraintViolations = new ArrayList<ConstraintViolationImpl<T>>();
		constraintTree.validateConstraints( value, executionContext, constraintViolations );
		if ( constraintViolations.size() > 0 ) {
			executionContext.addConstraintFailures( constraintViolations );
			return false;
		}
		return true;
	}

	private Type typeOfAnnoatedElement() {
		Type t;
		switch ( elementType ) {
			case TYPE: {
				t = type;
				break;
			}
			case METHOD: {
				t = ReflectionHelper.typeOf( method );
				break;
			}
			case FIELD: {
				t = ReflectionHelper.typeOf( field );
				break;
			}
			default: {
				throw new ValidationException(
						"Invalid state of MetaConstraint. Parameter elementType has unexpected value - " + elementType
				);
			}
		}
		return t;
	}
}
