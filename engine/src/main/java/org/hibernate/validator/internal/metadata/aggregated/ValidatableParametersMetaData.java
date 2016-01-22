/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.util.CollectionHelper;

/**
 * Represents the constraint related meta data of the arguments of a method or
 * constructor.
 *
 * @author Gunnar Morling
 */
public class ValidatableParametersMetaData implements Validatable {

	private final Iterable<Cascadable> cascadables;

	public ValidatableParametersMetaData(Iterable<? extends Cascadable> cascadables) {
		this.cascadables = CollectionHelper.<Cascadable>newHashSet( cascadables );
	}

	@Override
	public Iterable<Cascadable> getCascadables() {
		return cascadables;
	}
}
