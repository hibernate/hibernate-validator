/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.constraintvalidators.hv;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.UniqueElements;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.engine.messageinterpolation.util.InterpolationHelper;

import static java.util.stream.Collectors.toList;

/**
 * Validates that the provided collection contains unique elements, that no two elements in the collection are equal.
 * <p>
 * Uniqueness is defined by the equals method of the objects being compared.
 *
 * @author Tadhg Pearson
 */
public class UniqueElementsValidator implements ConstraintValidator<UniqueElements, Collection<?>> {

	/**
	 * @param objects Potentially null collection of input objects to check for duplicates
	 * @param validatorContext Non-null {@link ConstraintValidatorContext} to accumulate validation messages
	 *
	 * @return True if the input collection is null or does not contain duplicate objects
	 */
	@Override
	public boolean isValid(Collection<?> objects, ConstraintValidatorContext validatorContext) {
		boolean valid = isValid( objects );
		if ( !valid ) {
			List<String> duplicateElements = findDuplicates( objects );
			String duplicates = InterpolationHelper.escapeMessageParameter( duplicateElements.toString() );

			if ( validatorContext instanceof HibernateConstraintValidatorContext ) {
				validatorContext.unwrap( HibernateConstraintValidatorContext.class )
						.addMessageParameter( "duplicates", duplicates );
			}
		}
		return valid;
	}

	private boolean isValid(Collection<?> objects) {
		boolean valid;
		if ( objects == null ) {
			valid = true;
		}
		else {
			Set<?> set = new HashSet<>( objects );
			valid = set.size() == objects.size();
		}
		return valid;
	}


	private List<String> findDuplicates(Collection<?> objects) {
		Set<Object> uniqueElements = new HashSet<>();
		return objects.stream().filter( o -> !uniqueElements.add( o ) ).map( String::valueOf ).collect( toList() );
	}
}
