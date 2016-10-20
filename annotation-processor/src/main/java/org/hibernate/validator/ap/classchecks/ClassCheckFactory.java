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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.hibernate.validator.ap.util.CollectionHelper;

/**
 * A factory class that provides a collection of {@link ClassCheck}s based on the type of the element we need to check.
 *
 * @author Marko Bekhta
 */
public class ClassCheckFactory {

	/**
	 * Holds the checks to be executed for method elements.
	 */
	private final Collection<ClassCheck> methodChecks;

	public ClassCheckFactory(Types typeUtils, Elements elementUtils) {
		methodChecks = CollectionHelper.newArrayList();
		methodChecks.add( new ReturnValueMethodOverrideCheck( elementUtils, typeUtils ) );
		methodChecks.add( new ParametersMethodOverrideCheck( elementUtils, typeUtils ) );
	}

	public static ClassCheckFactory getInstance(Types typeUtils, Elements elementUtils) {
		return new ClassCheckFactory( typeUtils, elementUtils );
	}

	/**
	 * Provides a collections of checks to be performed on a given element.
	 *
	 * @param element an element you'd like to check
	 *
	 * @return The checks to be performed to validate the given
	 */
	public Collection<ClassCheck> getClassChecks(Element element) {
		switch ( element.getKind() ) {
			case METHOD:
				return methodChecks;
			default:
				return Collections.emptySet();
		}
	}
}
