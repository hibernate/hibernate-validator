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
package org.hibernate.validator.cfg.context.impl;

import java.lang.reflect.Member;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.metadata.location.BeanConstraintLocation;

/**
 * Constraint mapping creational context which allows to configure the constraints for one bean property.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class PropertyConstraintMappingContextImpl extends ConstraintMappingContextImplBase
		implements PropertyConstraintMappingContext {

	private final Member member;

	public PropertyConstraintMappingContextImpl(Class<?> beanClass, Member member, ConstraintMappingContext mapping) {

		super( beanClass, mapping );

		this.member = member;
	}

	public PropertyConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {

		mapping.addConstraintConfig(
				ConfiguredConstraint.forProperty(
						definition, beanClass, member
				)
		);
		return this;
	}

	public PropertyConstraintMappingContext valid() {
		mapping.addCascadeConfig( new BeanConstraintLocation( beanClass, member ) );
		return this;
	}
}

