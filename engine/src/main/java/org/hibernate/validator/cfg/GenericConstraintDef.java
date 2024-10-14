/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */

package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;

/**
 * A {@link ConstraintDef} class which can be used to configure any constraint
 * type. For this purpose the class defines a generic method
 * {@link GenericConstraintDef#param(String, Object)} which allows to add
 * arbitrary constraint parameters.
 *
 * @param <A> The constraint annotation type.
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
