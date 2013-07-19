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

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collections;

import org.hibernate.validator.cfg.ConstraintDef;
import org.hibernate.validator.cfg.context.MethodConstraintMappingContext;
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.location.BeanConstraintLocation;
import org.hibernate.validator.internal.metadata.location.ExecutableConstraintLocation;
import org.hibernate.validator.internal.metadata.raw.ConfigurationSource;
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement;
import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.metadata.raw.ConstrainedField;

/**
 * Constraint mapping creational context which allows to configure the constraints for one bean property.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 * @author Kevin Pollet <kevin.pollet@serli.com> (C) 2011 SERLI
 */
public final class PropertyConstraintMappingContextImpl extends ConstraintMappingContextImplBase
		implements PropertyConstraintMappingContext {

	private final TypeConstraintMappingContextImpl<?> typeContext;
	private final Member member;
	private boolean isCascading;

	public PropertyConstraintMappingContextImpl(TypeConstraintMappingContextImpl<?> typeContext, Member member) {
		super( typeContext.getConstraintMapping() );
		this.typeContext = typeContext;
		this.member = member;
	}

	@Override
	public PropertyConstraintMappingContext constraint(ConstraintDef<?, ?> definition) {
		if ( member instanceof Field ) {
			super.addConstraint(
					ConfiguredConstraint.forProperty(
							definition, member
					)
			);
		}
		else {
			super.addConstraint(
					ConfiguredConstraint.forReturnValue(
							definition, (Method) member
					)
			);
		}
		return this;
	}

	@Override
	public PropertyConstraintMappingContext ignoreAnnotations() {
		mapping.getAnnotationProcessingOptions().ignoreConstraintAnnotationsOnMember( member, true );
		return this;
	}

	@Override
	public PropertyConstraintMappingContext valid() {
		isCascading = true;
		return this;
	}

	@Override
	public PropertyConstraintMappingContext property(String property, ElementType elementType) {
		return typeContext.property( property, elementType );
	}

	@Override
	public MethodConstraintMappingContext method(String name, Class<?>... parameterTypes) {
		return typeContext.method( name, parameterTypes );
	}

	public ConstrainedElement build(ConstraintHelper constraintHelper) {
		if ( member instanceof Field ) {
			return new ConstrainedField(
					ConfigurationSource.API,
					new BeanConstraintLocation( member ),
					getConstraints( constraintHelper ),
					Collections.<Class<?>, Class<?>>emptyMap(),
					isCascading
			);
		}
		else {
			return new ConstrainedExecutable(
					ConfigurationSource.API,
					new ExecutableConstraintLocation( (Method) member ),
					getConstraints( constraintHelper ),
					isCascading
			);
		}
	}
}
