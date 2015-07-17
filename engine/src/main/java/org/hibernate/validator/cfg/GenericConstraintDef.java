/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;

/**
 * A {@link ConstraintDef} class which can be used to configure any constraint
 * type. For this purpose the class defines a generic method
 * {@link GenericConstraintDef#param(String, Object)} which allows to add
 * arbitrary constraint parameters.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public class GenericConstraintDef<A extends Annotation> extends ConstraintDef<GenericConstraintDef<A>, A> {

	public GenericConstraintDef(Class<A> constraintType) {
		super( constraintType );
	}

	public GenericConstraintDef<A> param(String key, Object value) {
		addParameter( key, value );
		return this;
	}
}
