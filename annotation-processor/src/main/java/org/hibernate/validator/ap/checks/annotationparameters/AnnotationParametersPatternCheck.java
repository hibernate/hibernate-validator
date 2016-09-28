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
import java.util.regex.PatternSyntaxException;

/**
 * Checks, that parameters used on {@code javax.validation.constraints.Pattern} annotation are valid.
 */
public class AnnotationParametersPatternCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersPatternCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, "javax.validation.constraints.Pattern" );
	}

	@Override
	protected Set<ConstraintCheckError> doCheck(Element element, AnnotationMirror annotation) {
		String regexp = (String) annotationApiHelper.getAnnotationValue( annotation, "regexp" ).getValue();

		try {
			Pattern.compile( regexp );
		} catch (PatternSyntaxException e) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "INVALID_PATTERN_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}
}
