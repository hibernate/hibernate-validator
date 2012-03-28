/*
* JBoss, Home of Professional Open Source
* Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.hibernate.validator.ap.checks;

import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.hibernate.validator.ap.util.CollectionHelper;
import org.hibernate.validator.ap.util.ConstraintHelper;

/**
 * A {@link ConstraintChecks} implementation, that executed the contained checks
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

	public Set<ConstraintCheckError> execute(Element element,
											 AnnotationMirror annotation) {

		Set<ConstraintCheckError> theValue = CollectionHelper.newHashSet();

		//execute the checks on each element of the multi-valued constraint
		for ( AnnotationMirror onePartOfMultiValuedConstraint :
				constraintHelper.getPartsOfMultiValuedConstraint( annotation ) ) {

			theValue.addAll( delegate.execute( element, onePartOfMultiValuedConstraint ) );
		}

		return theValue;
	}

}
