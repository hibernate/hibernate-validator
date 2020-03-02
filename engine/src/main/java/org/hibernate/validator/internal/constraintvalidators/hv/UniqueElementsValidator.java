/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.UniqueElements;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.CollectionHelper;

/**
 * Validates that the provided collection only contains unique elements, i.e. that we can't find 2 equal elements in the
 * collection.
 * <p>
 * Uniqueness is defined by the {@code equals()} method of the objects being compared.
 *
 * @author Tadhg Pearson
 * @author Guillaume Smet
 */
@SuppressWarnings("rawtypes")
// as per the JLS, Collection<?> is a subtype of Collection, so we need to explicitly reference
// Collection here to support having properties defined as Collection (see HV-1551)
public class UniqueElementsValidator implements ConstraintValidator<UniqueElements, Collection> {

	/**
	 * @param collection the collection to validate
	 * @param constraintValidatorContext context in which the constraint is evaluated
	 *
	 * @return true if the input collection is null or does not contain duplicate elements
	 */
	@Override
	public boolean isValid(Collection collection, ConstraintValidatorContext constraintValidatorContext) {
		if ( collection == null || collection.size() < 2 ) {
			return true;
		}

		List<Object> duplicates = findDuplicates( collection );

		if ( duplicates.isEmpty() ) {
			return true;
		}

		if ( constraintValidatorContext instanceof HibernateConstraintValidatorContext ) {
			constraintValidatorContext.unwrap( HibernateConstraintValidatorContext.class )
					.addMessageParameter( "duplicates", duplicates.stream().map( String::valueOf ).collect( Collectors.joining( ", " ) ) )
					.withDynamicPayload( CollectionHelper.toImmutableList( duplicates ) );
		}

		return false;
	}

	private List<Object> findDuplicates(Collection<?> collection) {
		Set<Object> uniqueElements = CollectionHelper.newHashSet( collection.size() );
		return collection.stream().filter( o -> !uniqueElements.add( o ) )
				.collect( toList() );
	}
}
