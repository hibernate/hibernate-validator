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

/**
 * Constraint mapping creational context which allows to configure the constraints for one bean property.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class PropertyConstraintMappingCreationalContext extends ConstraintMappingCreationalContextImplBase
		implements Constrainable<PropertyConstraintMappingCreationalContext>, TypeTargets, Cascadable<PropertyConstraintMappingCreationalContext> {

	private final String property;
	private final ElementType elementType;

	public PropertyConstraintMappingCreationalContext(Class<?> beanClass, String property, ElementType elementType, ConstraintMapping mapping) {

		super( beanClass, mapping );

		this.property = property;
		this.elementType = elementType;
	}

	public PropertyConstraintMappingCreationalContext constraint(ConstraintDef<?, ?> definition) {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		GenericConstraintDef<?> constraintDefinition = new GenericConstraintDef(
				beanClass, definition.constraintType, property, elementType, definition.parameters, mapping
		);

		mapping.addConstraintConfig( constraintDefinition );

		return this;
	}

	public <A extends Annotation> PropertyConstraintMappingCreationalContext constraint(GenericConstraintDef<A> definition) {
		final GenericConstraintDef<A> constraintDefinition = new GenericConstraintDef<A>(
				beanClass, definition.constraintType, property, elementType, mapping
		);
		constraintDefinition.parameters.putAll( definition.parameters );

		mapping.addConstraintConfig( constraintDefinition );

		return this;
	}

	public PropertyConstraintMappingCreationalContext valid() {
		mapping.addCascadeConfig( new CascadeDef( beanClass, property, elementType ) );
		return this;
	}

}

