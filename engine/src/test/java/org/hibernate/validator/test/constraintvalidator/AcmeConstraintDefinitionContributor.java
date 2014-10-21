/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.test.constraintvalidator;

import org.hibernate.validator.spi.constraintdefinition.ConstraintDefinitionContributor;

/**
 * @author Hardy Ferentschik
 */
public class AcmeConstraintDefinitionContributor implements ConstraintDefinitionContributor {
	private final boolean keepDefaults;

	public AcmeConstraintDefinitionContributor() {
		this( true );
	}

	public AcmeConstraintDefinitionContributor(boolean keepDefaults) {
		this.keepDefaults = keepDefaults;
	}

	@Override
	public void collectConstraintDefinitions(ConstraintDefinitionBuilder constraintDefinitionContributionBuilder) {
		constraintDefinitionContributionBuilder.constraint( AcmeConstraint.class )
				.includeExistingValidators( keepDefaults )
				.validatedBy( AcmeConstraint.AcmeConstraintValidator.class )
				.constraint( AcmeConstraintWithDefaultValidator.class )
				.includeExistingValidators( keepDefaults )
				.validatedBy( AcmeConstraintWithDefaultValidator.DefaultAcmeConstraintValidator.class );
	}
}


