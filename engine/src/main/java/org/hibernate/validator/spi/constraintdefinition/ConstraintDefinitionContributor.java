/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.constraintdefinition;

import java.lang.annotation.Annotation;
import javax.validation.ConstraintValidator;

/**
 * A {@code ConstraintDefinitionContributor} allows for the contribution of custom constraint validator instances.
 *
 * This is a Hibernate Validator specific feature and can be configured via
 * {@link org.hibernate.validator.HibernateValidatorConfiguration}.
 *
 * <p>
 * The default implementation uses Java's {@code ServiceLoader} approach to discover custom constraint validator
 * implementations.
 * </p>
 *
 * @author Hardy Ferentschik
 * @hv.experimental This API is considered experimental and may change in future revisions
 */
public interface ConstraintDefinitionContributor {

	/**
	 * Callback for registering additional validators for given constraints. Use the provided builder object to
	 * configure the validators.
	 *
	 * @param constraintDefinitionContributionBuilder Builder to contribute constraint validators using a fluent API.
	 *
	 */
	void collectConstraintDefinitions(ConstraintDefinitionBuilder constraintDefinitionContributionBuilder);

	/**
	 * Allows to register the validators applying for given constraints, following a fluent API pattern.
	 */
	public interface ConstraintDefinitionBuilder {

		<A extends Annotation> ConstraintDefinitionBuilderContext<A> constraint(Class<A> constraintType);
	}

	/**
	 * Allows to register the validators applying for one given constraint.
	 *
	 * @param <A> the constraint type
	 */
	public interface ConstraintDefinitionBuilderContext<A extends Annotation> {
		ConstraintDefinitionBuilderContext<A> validatedBy(Class<? extends ConstraintValidator<A, ?>> validatorType);

		ConstraintDefinitionBuilderContext<A> includeExistingValidators(boolean include);

		<B extends Annotation> ConstraintDefinitionBuilderContext<B> constraint(Class<B> constraintType);
	}
}
