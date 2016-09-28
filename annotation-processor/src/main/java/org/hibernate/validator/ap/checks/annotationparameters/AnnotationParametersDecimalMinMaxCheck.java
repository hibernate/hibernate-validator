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
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Checks, that parameters used on {@code javax.validation.constraints.DecimalMin} and {@code javax.validation.constraints.DecimalMax} annotation are valid.
 */
public class AnnotationParametersDecimalMinMaxCheck extends AnnotationParametersAbstractCheck {


	/*
	 * Translates the string representation of a BigDecimal into a BigDecimal. The string representation consists of an optional sign, '+' ( '\u002B') or '-' ('\u002D'), followed by a sequence of zero or more decimal digits ("the integer"), optionally followed by a fraction, optionally followed by an exponent.
	 * The fraction consists of a decimal point followed by zero or more decimal digits. The string must contain at least one digit in either the integer or the fraction. The number formed by the sign, the integer and the fraction is referred to as the significand.
	 *
	 * The exponent consists of the character 'e' ('\u0065') or 'E' ('\u0045') followed by one or more decimal digits. The value of the exponent must lie between -Integer.MAX_VALUE (Integer.MIN_VALUE+1) and Integer.MAX_VALUE, inclusive.
	 */
	private static Pattern BIG_DECIMAL_PATTERN = Pattern.compile( "[+-]?[0-9]*((\\.[0-9]*)?([eE][+-]?[0-9]*)?)?" );

	public AnnotationParametersDecimalMinMaxCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, "javax.validation.constraints.DecimalMin", "javax.validation.constraints.DecimalMax" );
	}

	@Override
	protected Set<ConstraintCheckError> doCheck(Element element, AnnotationMirror annotation) {
		String value = (String) annotationApiHelper.getAnnotationValue( annotation, "value" ).getValue();

		if ( !BIG_DECIMAL_PATTERN.matcher( value ).matches() ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "INVALID_DECIMAL_MIN_MAX_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}
}
