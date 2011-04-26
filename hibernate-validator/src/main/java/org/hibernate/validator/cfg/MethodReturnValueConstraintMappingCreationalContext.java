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

import static java.lang.annotation.ElementType.METHOD;

/**
 * Constraint mapping creational context which allows to configure the constraints for one method return value.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class MethodReturnValueConstraintMappingCreationalContext
		extends ConstraintMappingCreationalContextImplBase
		implements TypeTargets, ParameterTarget, Constrainable<MethodReturnValueConstraintMappingCreationalContext>, Cascadable<MethodReturnValueConstraintMappingCreationalContext> {

	private final String methodName;
	private final Class<?>[] parameterTypes;

	public MethodReturnValueConstraintMappingCreationalContext(Class<?> beanClass, String methodName, Class<?>[] parameterTypes, ConstraintMapping mapping) {

		super( beanClass, mapping );

		this.methodName = methodName;
		this.parameterTypes = parameterTypes;
	}

	public MethodReturnValueConstraintMappingCreationalContext constraint(ConstraintDef<?, ?> definition) {
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	public <A extends Annotation> MethodReturnValueConstraintMappingCreationalContext constraint(GenericConstraintDef<A> definition) {
		throw new UnsupportedOperationException( "Not implemented yet" );
	}

	/**
	 * Marks the current property as cascadable.
	 *
	 * @return Returns itself for method chaining.
	 */
	public MethodReturnValueConstraintMappingCreationalContext valid() {
		mapping.addMethodCascadeConfig( new MethodCascadeDef( beanClass, methodName, parameterTypes, 0, METHOD ) );
		return this;
	}

	/**
	 * Changes the parameter for which added constraints apply.
	 *
	 * @param index The parameter index.
	 *
	 * @return Returns a new {@code ConstraintsForTypeMethodElement} instance allowing method chaining.
	 */
	public MethodParameterConstraintMappingCreationalContext parameter(int index) {
		return new MethodParameterConstraintMappingCreationalContext(
				beanClass, methodName, parameterTypes, index, mapping
		);
	}

}
