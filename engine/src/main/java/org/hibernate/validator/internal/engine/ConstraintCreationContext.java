/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.engine;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorManager;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.util.TypeResolutionHelper;

public class ConstraintCreationContext {

	private final ConstraintHelper constraintHelper;

	private final ConstraintValidatorManager constraintValidatorManager;

	private final TypeResolutionHelper typeResolutionHelper;

	private final ValueExtractorManager valueExtractorManager;

	public ConstraintCreationContext(ConstraintHelper constraintHelper,
			ConstraintValidatorManager constraintValidatorManager,
			TypeResolutionHelper typeResolutionHelper,
			ValueExtractorManager valueExtractorManager) {
		this.constraintHelper = constraintHelper;
		this.constraintValidatorManager = constraintValidatorManager;
		this.typeResolutionHelper = typeResolutionHelper;
		this.valueExtractorManager = valueExtractorManager;
	}

	public ConstraintHelper getConstraintHelper() {
		return constraintHelper;
	}

	public ConstraintValidatorManager getConstraintValidatorManager() {
		return constraintValidatorManager;
	}

	public TypeResolutionHelper getTypeResolutionHelper() {
		return typeResolutionHelper;
	}

	public ValueExtractorManager getValueExtractorManager() {
		return valueExtractorManager;
	}
}
