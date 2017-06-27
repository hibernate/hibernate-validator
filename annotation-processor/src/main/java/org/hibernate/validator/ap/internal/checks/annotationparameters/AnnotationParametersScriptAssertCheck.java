/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.internal.checks.annotationparameters;

import java.util.Collections;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import org.hibernate.validator.ap.internal.checks.ConstraintCheckIssue;
import org.hibernate.validator.ap.internal.util.AnnotationApiHelper;
import org.hibernate.validator.ap.internal.util.CollectionHelper;
import org.hibernate.validator.ap.internal.util.TypeNames;

/**
 * Checks that the parameters used on {@code org.hibernate.validator.constraints.ScriptAssert} annotations are valid.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersScriptAssertCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersScriptAssertCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, TypeNames.HibernateValidatorTypes.SCRIPT_ASSERT );
	}

	@Override
	protected Set<ConstraintCheckIssue> doCheck(Element element, AnnotationMirror annotation) {
		String lang = (String) annotationApiHelper.getAnnotationValue( annotation, "lang" ).getValue();
		String script = (String) annotationApiHelper.getAnnotationValue( annotation, "script" ).getValue();
		String alias = annotationApiHelper.getAnnotationValue( annotation, "alias" ) != null ?
				(String) annotationApiHelper.getAnnotationValue( annotation, "alias" ).getValue() : "_this";

		if ( ( lang.trim().length() == 0 ) || ( script.trim().length() == 0 ) || ( alias.trim().length() == 0 ) ) {
			return CollectionHelper.asSet(
					ConstraintCheckIssue.error(
							element, annotation, "INVALID_SCRIPT_ASSERT_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}
}
