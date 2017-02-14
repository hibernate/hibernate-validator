/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;

/**
 * Represents the constraint related meta data of the arguments of a method or
 * constructor.
 *
 * @author Gunnar Morling
 */
public class ValidatableParametersMetaData implements Validatable {

	@SuppressWarnings("unused")
	private final List<ParameterMetaData> parameterMetaData;
	private final Iterable<Cascadable> cascadables;

	public ValidatableParametersMetaData(List<ParameterMetaData> parameterMetaData) {
		this.parameterMetaData = Collections.unmodifiableList( parameterMetaData );
		this.cascadables = Collections.unmodifiableList( parameterMetaData.stream()
				.filter( p -> p.isCascading() )
				.collect( Collectors.toList() ) );
	}

	@Override
	public Iterable<Cascadable> getCascadables() {
		return cascadables;
	}

}
