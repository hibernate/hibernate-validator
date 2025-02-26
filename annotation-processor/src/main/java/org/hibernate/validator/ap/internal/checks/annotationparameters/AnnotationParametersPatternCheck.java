/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.ap.internal.checks.annotationparameters;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.TypeNames;

/**
 * Checks that the parameters used on {@code jakarta.validation.constraints.Pattern} annotations are valid.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersPatternCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersPatternCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, TypeNames.BeanValidationTypes.PATTERN );
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {
		String regexp = (String) annotationApiHelper.getAnnotationValue( annotation, "regexp" ).getValue();

		try {
			Pattern.compile( regexp );
		}
		catch (PatternSyntaxException e) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, "INVALID_PATTERN_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}
}
