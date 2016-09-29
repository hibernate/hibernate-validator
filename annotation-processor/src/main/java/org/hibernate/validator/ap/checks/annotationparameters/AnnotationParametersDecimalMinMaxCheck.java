/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks.annotationparameters;

import org.hibernate.validator.ap.checks.ConstraintCheckError;
import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

/**
 * Checks, that parameters used on {@code javax.validation.constraints.DecimalMin} and {@code javax.validation.constraints.DecimalMax} annotation are valid.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersDecimalMinMaxCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersDecimalMinMaxCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, "javax.validation.constraints.DecimalMin", "javax.validation.constraints.DecimalMax" );
	}

	@Override
	protected Set<ConstraintCheckError> doCheck(Element element, AnnotationMirror annotation) {
		String value = (String) annotationApiHelper.getAnnotationValue( annotation, "value" ).getValue();

		try {
			new BigDecimal( value );
			return Collections.emptySet();
		}
		catch (NumberFormatException nfe) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "INVALID_DECIMAL_MIN_MAX_ANNOTATION_PARAMETERS"
					)
			);
		}

	}
}
