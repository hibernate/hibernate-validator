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

import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContribution;
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


