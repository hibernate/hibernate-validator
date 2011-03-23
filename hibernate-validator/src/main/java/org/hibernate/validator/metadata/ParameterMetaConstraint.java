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
package org.hibernate.validator.metadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.hibernate.validator.metadata.location.MethodParameterConstraintLocation;

/**
 * A {@link MetaConstraint} implementation that represents a method parameter constraint.
 *
 * @author Gunnar Morling
 */
public class ParameterMetaConstraint<T, A extends Annotation> extends MetaConstraint<A> {

	/**
	 * Creates a new {@link ParameterMetaConstraint} representing a method
	 * parameter constraint.
	 *
	 * @param constraintDescriptor The descriptor for this constraint.
	 * @param method The method hosting this constraint.
	 * @param parameterIndex The index of the parameter hosting this constraint.
	 */
	public ParameterMetaConstraint(
			ConstraintDescriptorImpl<A> constraintDescriptor, Method method,
			int parameterIndex) {

		super( constraintDescriptor, new MethodParameterConstraintLocation( method, parameterIndex ) );
	}
}
