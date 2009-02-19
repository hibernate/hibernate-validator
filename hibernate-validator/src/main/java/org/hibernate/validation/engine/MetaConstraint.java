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

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintDescriptor;
import javax.validation.ValidationException;
import javax.validation.groups.Default;

import org.hibernate.validation.util.ReflectionHelper;

/**
 * Instances of this class abstract the constraint type  (class, method or field constraint) and gives access to
 * meta data about the constraint. This allows a unified handling of constraints in the validator imlpementation.
 *
 * @author Hardy Ferentschik
 */
public class MetaConstraint {

	/**
	 * The constraint tree created from the constraint annotation.
	 */
	private final ConstraintTree constraintTree;

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

	private final List<Class<?>> groupList;

	public MetaConstraint(Type t, ConstraintDescriptor constraintDescriptor, List<Class<?>> defaultGroupList) {
		this( t, null, null, ElementType.FIELD, constraintDescriptor, "", defaultGroupList );
	}

	public MetaConstraint(Method m, ConstraintDescriptor constraintDescriptor, List<Class<?>> defaultGroupList) {
		this(
				null,
				m,
				null,
				ElementType.METHOD,
				constraintDescriptor,
				ReflectionHelper.getPropertyName( m ),
				defaultGroupList
		);
	}

	public MetaConstraint(Field f, ConstraintDescriptor constraintDescriptor, List<Class<?>> defaultGroupList) {
		this( null, null, f, ElementType.FIELD, constraintDescriptor, f.getName(), defaultGroupList );
	}

	private MetaConstraint(Type t, Method m, Field f, ElementType elementType, ConstraintDescriptor constraintDescriptor, String property, List<Class<?>> defaultGroupList) {
		this.type = t;
		this.method = m;
		this.field = f;
		this.elementType = elementType;
		this.propertyName = property;
		constraintTree = new ConstraintTree( constraintDescriptor );
		this.groupList = new ArrayList<Class<?>>( constraintDescriptor.getGroups() );
		checkIfPartOfDefaultGroup( defaultGroupList );
	}

	private void checkIfPartOfDefaultGroup(List<Class<?>> defaultGroupList) {
		for ( Class<?> clazz : defaultGroupList ) {
			if ( groupList.contains( clazz ) ) {
				groupList.add( Default.class );
				break;
			}
		}
	}

	/**
	 * @return Returns the list of groups this constraint is part of. This might include the default group even when
	 * it is not explicitly specified, but part of the redefined default group list of the hosting bean.
	 */
	public List<Class<?>> getGroupList() {
		return groupList;
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

	public String getPropertyName() {
		return propertyName;
	}

	public ElementType getElementType() {
		return elementType;
	}

	public ConstraintTree getConstraintTree() {
		return constraintTree;
	}

	public <T> void validateConstraint(Class beanClass, ExecutionContext<T> executionContext) {
		final Object leafBeanInstance = executionContext.peekValidatedObject();
		Object value = getValue( leafBeanInstance );
		constraintTree.validateConstraints( value, beanClass, executionContext );
	}

	public <T> void validateConstraint(Class beanClass, Object value, ExecutionContext<T> executionContext) {
		constraintTree.validateConstraints( value, beanClass, executionContext );
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
