/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.ap.classchecks;

import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import org.hibernate.validator.ap.checks.ConstraintCheckIssue;

/**
 * <p>
 * Abstract base class for {@link ClassCheck} implementations. Concrete
 * checks should only override those check methods applicable for their
 * supported element types.
 * </p>
 * <p>
 * All check methods not overridden will return an empty list.
 * </p>
 *
 * @author Marko Bekhta
 */
public abstract class AbstractClassCheck implements ClassCheck {

	@Override
	public Collection<ConstraintCheckIssue> checkMethod(ExecutableElement element) {
		return Collections.emptySet();
	}

	public final Collection<ConstraintCheckIssue> execute(Element element) {
		switch ( element.getKind() ) {
			case METHOD:
				return checkMethod( (ExecutableElement) element );
			default:
				return Collections.emptySet();
		}
	}

}
