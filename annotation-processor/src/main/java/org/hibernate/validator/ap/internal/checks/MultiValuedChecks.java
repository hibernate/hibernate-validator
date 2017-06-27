/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks;

import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.ConstraintHelper;

/**
 * A {@link ConstraintChecks} implementation that executes the contained checks
 * against all parts of given multi-valued annotations.
 *
 * @author Gunnar Morling
 */
public class MultiValuedChecks implements ConstraintChecks {

	private final ConstraintHelper constraintHelper;

	private final SingleValuedChecks delegate;

	/**
	 * Creates a new MultiValuedChecks.
	 *
	 * @param constraintHelper Helper for handling multi-valued constraints.
	 * @param checks The checks to execute.
	 */
	public MultiValuedChecks(ConstraintHelper constraintHelper,
							 ConstraintCheck... checks) {

		this.constraintHelper = constraintHelper;
		this.delegate = new SingleValuedChecks( checks );
	}

	@Override
	public Set<ConstraintCheckIssue> execute(Element element, AnnotationMirror annotation) {
		Set<ConstraintCheckIssue> theValue = CollectionHelper.newHashSet();

		//execute the checks on each element of the multi-valued constraint
		for ( AnnotationMirror onePartOfMultiValuedConstraint :
				constraintHelper.getPartsOfMultiValuedConstraint( annotation ) ) {

			theValue.addAll( delegate.execute( element, onePartOfMultiValuedConstraint ) );
		}

		return theValue;
	}

}
