/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.validator.internal.metadata.facets.Cascadable;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.util.CollectionHelper;
import org.hibernate.validator.internal.util.stereotypes.Immutable;

/**
 * Represents the constraint related meta data of the arguments of a method or
 * constructor.
 *
 * @author Gunnar Morling
 */
public class ValidatableParametersMetaData implements Validatable {

	@Immutable
	private final List<ParameterMetaData> parameterMetaData;

	@Immutable
	private final List<Cascadable> cascadables;

	public ValidatableParametersMetaData(List<ParameterMetaData> parameterMetaData) {
		this.parameterMetaData = CollectionHelper.toImmutableList( parameterMetaData );
		this.cascadables = CollectionHelper.toImmutableList( parameterMetaData.stream()
				.filter( p -> p.isCascading() )
				.collect( Collectors.toList() ) );
	}

	@Override
	public Iterable<Cascadable> getCascadables() {
		return cascadables;
	}

	@Override
	public boolean hasCascadables() {
		return !cascadables.isEmpty();
	}
}
