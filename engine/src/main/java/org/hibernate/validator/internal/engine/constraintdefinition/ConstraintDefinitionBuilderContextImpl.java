/*
* JBoss, Home of Professional Open Source
* Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual contributors
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
package org.hibernate.validator.internal.engine.constraintdefinition;

import java.lang.annotation.Annotation;
import java.util.List;
import javax.validation.ConstraintValidator;

import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContribution;
import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor.ConstraintDefinitionBuilder;
import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor.ConstraintDefinitionBuilderContext;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * @author Gunnar Morling
 */
class ConstraintDefinitionBuilderContextImpl<A extends Annotation> implements ConstraintDefinitionBuilderContext<A> {

	private final ConstraintDefinitionBuilder builder;
	private final Class<A> constraintType;
	private boolean includeExistingValidators = true;
	private final List<Class<? extends ConstraintValidator<A, ?>>> validatorTypes = newArrayList();

	public ConstraintDefinitionBuilderContextImpl(ConstraintDefinitionBuilder builder, Class<A> constraintType) {
		this.builder = builder;
		this.constraintType = constraintType;
	}

	@Override
	public ConstraintDefinitionBuilderContext<A> includeExistingValidators(boolean include) {
		this.includeExistingValidators = include;
		return this;
	}

	@Override
	public ConstraintDefinitionBuilderContext<A> validatedBy(Class<? extends ConstraintValidator<A, ?>> validatorType) {
		validatorTypes.add( validatorType );
		return this;
	}

	@Override
	public <B extends Annotation> ConstraintDefinitionBuilderContext<B> constraint(Class<B> constraintType) {
		return builder.constraint( constraintType );
	}

	ConstraintDefinitionContribution<?> build() {
		return new ConstraintDefinitionContribution<A>( constraintType, validatorTypes, includeExistingValidators );
	}
}



