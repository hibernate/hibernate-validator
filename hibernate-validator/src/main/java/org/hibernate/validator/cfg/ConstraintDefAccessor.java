/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.util.Map;

/**
 * Provides public access to the fields of {@link ConstraintDef}.
 * ConstraintDef's fields are intentionally made protected instead of private in
 * order to avoid the need for getter methods, which would pollute
 * ConstraintDef's fluent API.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public final class ConstraintDefAccessor<A extends Annotation> extends ConstraintDef<ConstraintDefAccessor<A>, A> {

	/**
	 * @param <A>
	 *            The constraint annotation type of the accessor to create.
	 * 
	 *            Creates a new accessor for the given constraint definition.
	 * 
	 * @param originalDef
	 *            The constraint definition to provide access to.
	 */
	public static <A extends Annotation> ConstraintDefAccessor<A> getInstance(ConstraintDef<?, A> originalDef) {
		return new ConstraintDefAccessor<A>( originalDef );
	}

	private ConstraintDefAccessor(ConstraintDef<?, A> originalDef) {
		super(
				originalDef.beanType,
				originalDef.constraintType,
				originalDef.property,
				originalDef.elementType,
				originalDef.parameters,
				originalDef.mapping
		);
	}

	/**
	 * @return The constraint annotation type of this definition.
	 */
	public Class<A> getConstraintType() {
		return constraintType;
	}

	/**
	 * @return A map with the annotation parameters of this definition. Keys are property names of
	 *         this definition's annotation type, values are annotation parameter values of the appropriate types.
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}

	/**
	 * @return The bean type of this definition.
	 */
	public Class<?> getBeanType() {
		return beanType;
	}

	/**
	 * @return The element type of this definition.
	 */
	public ElementType getElementType() {
		return elementType;
	}

	/**
	 * @return The property name of this definition, if it represents a property level constraint.
	 */
	public String getProperty() {
		return property;
	}
}
