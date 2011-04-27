/*
* JBoss, Home of Professional Open Source
* Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual contributors
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

import static java.lang.annotation.ElementType.TYPE;

/**
 * Represents a programmatically configured constraint and meta-data
 * related to its location (bean type etc.).
 * 
 * @author Gunnar Morling
 */
public class ConfiguredConstraint<A extends Annotation> {

	private static final String EMPTY_PROPERTY = "";
	
	private final ConstraintDef<?, A> constraint;
	private final Class<?> beanType;
	private final String property;
	private final ElementType elementType;
	private final Class<A> constraintType;
	private final Map<String, Object> parameters;

	private ConfiguredConstraint(ConstraintDef<?, A> constraint, Class<?> beanType,
								String property, ElementType elementType) {

		this.constraint = constraint;
		this.beanType = beanType;
		this.property = property;
		this.elementType = elementType;

		this.constraintType = constraint.constraintType;
		this.parameters = constraint.parameters;
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forType(ConstraintDef<?, A> constraint, Class<?> beanType) {
		return new ConfiguredConstraint<A>(constraint, beanType, EMPTY_PROPERTY, TYPE);
	}

	public static <A extends Annotation> ConfiguredConstraint<A> forProperty (ConstraintDef<?, A> constraint, Class<?> beanType, String property, ElementType elementType) {
		return new ConfiguredConstraint<A>(constraint, beanType, property, elementType);
	}
	
	public ConstraintDef<?, A> getConstraint() {
		return constraint;
	}

	public Class<?> getBeanType() {
		return beanType;
	}

	public String getProperty() {
		return property;
	}

	public ElementType getElementType() {
		return elementType;
	}

	public Class<A> getConstraintType() {
		return constraintType;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

}
