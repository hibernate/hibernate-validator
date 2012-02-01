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
package org.hibernate.validator.internal.cfg.context;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.internal.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.internal.metadata.location.MethodConstraintLocation;

/**
 * Constraint mapping creational context which allows to configure the constraints for one bean property.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class PropertyConstraintMappingContextImpl extends ConstraintMappingContextImplBase
		implements PropertyConstraintMappingContext {

	private final Member member;

	public PropertyConstraintMappingContextImpl(Class<?> beanClass, Member member, ConstraintMappingContext mapping) {

		super( beanClass, mapping );

		this.member = member;
	}

	public PropertyConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {

		if ( member instanceof Field ) {
			mapping.addConstraintConfig(
					ConfiguredConstraint.forProperty(
							definition, member
					)
			);
		}
		else {
			mapping.addMethodConstraintConfig(
					ConfiguredConstraint.forReturnValue(
							definition, (Method) member
					)
			);
		}
		return this;
	}

	public PropertyConstraintMappingContext valid() {

		if ( member instanceof Field ) {
			mapping.addCascadeConfig( new BeanConstraintLocation( member ) );
		}
		else {
			mapping.addMethodCascadeConfig( new MethodConstraintLocation( (Method) member ) );
		}

		return this;
	}
}

