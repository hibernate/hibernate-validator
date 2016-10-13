/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.checks.annotationparameters;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.hibernate.validator.ap.checks.ConstraintCheckError;
import org.hibernate.validator.ap.util.AnnotationApiHelper;
import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * Checks that the parameters used on {@code org.hibernate.validator.constraints.NumberPattern} annotations are valid.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersNumberPatternCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersNumberPatternCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, "org.hibernate.validator.constraints.NumberPattern" );
	}

	@Override
	protected Set<ConstraintCheckError> doCheck(Element element, AnnotationMirror annotation) {
		String regexp = (String) annotationApiHelper.getAnnotationValue( annotation, "regexp" ).getValue();
		String numberFormat = annotationApiHelper.getAnnotationValue( annotation, "numberFormat" ) != null ?
				(String) annotationApiHelper.getAnnotationValue( annotation, "numberFormat" ).getValue() : "";

		try {
			Pattern.compile( regexp );
		}
		catch (PatternSyntaxException e) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "INVALID_PATTERN_ANNOTATION_PARAMETERS"
					)
			);
		}
		if ( !numberFormat.trim().isEmpty() ) {
			try {
				new DecimalFormat( numberFormat );
			}
			catch (IllegalArgumentException e) {
				return CollectionHelper.asSet(
						new ConstraintCheckError(
								element, annotation, "INVALID_NUMBER_PATTERN_ANNOTATION_PARAMETERS"
						)
				);
			}
		}

		return Collections.emptySet();
	}
}
