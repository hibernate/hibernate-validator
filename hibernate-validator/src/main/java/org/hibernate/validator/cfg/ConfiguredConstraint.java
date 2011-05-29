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
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;

import org.hibernate.validator.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.metadata.location.ConstraintLocation;
import org.hibernate.validator.metadata.location.ParameterConstraintLocation;
import org.hibernate.validator.util.ReflectionHelper;

/**
 * Represents a programmatically configured constraint and meta-data
 * related to its location (bean type etc.).
 *
 * @author Gunnar Morling
 */
public class ConfiguredConstraint<A extends Annotation, L extends ConstraintLocation> {

	private final ConstraintDef<?, A> constraint;
	private final L location;

	private ConfiguredConstraint(ConstraintDef<?, A> constraint, L location) {

		this.constraint = constraint;
		this.location = location;
	}

	public static <A extends Annotation> ConfiguredConstraint<A, BeanConstraintLocation> forType(ConstraintDef<?, A> constraint, Class<?> beanType) {
		return new ConfiguredConstraint<A, BeanConstraintLocation>(
				constraint, new BeanConstraintLocation( beanType )
		);
	}

	public static <A extends Annotation> ConfiguredConstraint<A, BeanConstraintLocation> forProperty(ConstraintDef<?, A> constraint, Class<?> beanType, String property, ElementType elementType) {

		Member member = ReflectionHelper.getMember( beanType, property, elementType );

		return new ConfiguredConstraint<A, BeanConstraintLocation>(
				constraint, new BeanConstraintLocation( beanType, member )
		);
	}

	public static <A extends Annotation> ConfiguredConstraint<A, ParameterConstraintLocation> forParameter(ConstraintDef<?, A> constraint, Method method, int parameterIndex) {
		return new ConfiguredConstraint<A, ParameterConstraintLocation>(
				constraint, new ParameterConstraintLocation( method, parameterIndex )
		);
	}

	public ConstraintDef<?, A> getConstraint() {
		return constraint;
	}

	public L getLocation() {
		return location;
	}

	public Class<A> getConstraintType() {
		return constraint.constraintType;
	}

	public Map<String, Object> getParameters() {
		return constraint.parameters;
	}

}
