/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine.constraintdefinition;

import java.lang.annotation.Annotation;
import java.util.List;

import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor.ConstraintDefinitionBuilder;
import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor.ConstraintDefinitionBuilderContext;

import static org.hibernate.validator.internal.util.CollectionHelper.newArrayList;

/**
 * @author Gunnar Morling
 */
public class ConstraintDefinitionBuilderImpl implements ConstraintDefinitionBuilder {

	private final List<ConstraintDefinitionBuilderContextImpl<?>> contexts = newArrayList();

	@Override
	public <A extends Annotation> ConstraintDefinitionBuilderContext<A> constraint(Class<A> constraintType) {
		ConstraintDefinitionBuilderContextImpl<A> context = new ConstraintDefinitionBuilderContextImpl<A>(
				this, constraintType
		);
		contexts.add( context );
		return context;
	}

	public List<ConstraintDefinitionContribution<?>> getConstraintValidatorContributions() {
		List<ConstraintDefinitionContribution<?>> contributions = newArrayList( contexts.size() );

		for ( ConstraintDefinitionBuilderContextImpl<?> context : contexts ) {
			contributions.add( context.build() );
		}

		return contributions;
	}
}


