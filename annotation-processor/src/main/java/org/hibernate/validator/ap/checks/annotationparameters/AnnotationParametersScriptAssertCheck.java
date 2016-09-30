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

/**
 * Checks that the parameters used on {@code org.hibernate.validator.constraints.ScriptAssert} annotations are valid.
 *
 * @author Marko Bekhta
 */
public class AnnotationParametersScriptAssertCheck extends AnnotationParametersAbstractCheck {

	public AnnotationParametersScriptAssertCheck(AnnotationApiHelper annotationApiHelper) {
		super( annotationApiHelper, "org.hibernate.validator.constraints.ScriptAssert" );
	}

	@Override
	protected Set<ConstraintCheckError> doCheck(Element element, AnnotationMirror annotation) {
		String lang = (String) annotationApiHelper.getAnnotationValue( annotation, "lang" ).getValue();
		String script = (String) annotationApiHelper.getAnnotationValue( annotation, "script" ).getValue();
		String alias = annotationApiHelper.getAnnotationValue( annotation, "alias" ) != null ?
				(String) annotationApiHelper.getAnnotationValue( annotation, "alias" ).getValue() : "_this";

		if ( ( lang.trim().length() == 0 ) || ( script.trim().length() == 0 ) || ( alias.trim().length() == 0 ) ) {
			return CollectionHelper.asSet(
					new ConstraintCheckError(
							element, annotation, "INVALID_SCRIPT_ASSERT_ANNOTATION_PARAMETERS"
					)
			);
		}

		return Collections.emptySet();
	}
}
