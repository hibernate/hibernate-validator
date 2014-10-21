/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintdefinition;

import java.lang.annotation.Annotation;
import java.util.List;
import javax.validation.ConstraintValidator;

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



