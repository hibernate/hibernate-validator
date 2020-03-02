/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.cfg;

import java.lang.annotation.Annotation;

import jakarta.validation.Payload;

import org.hibernate.validator.internal.metadata.core.ConstraintHelper;

/**
 * Base class for all constraint definition types. Each sub type represents a
 * single constraint annotation type and allows to add this constraint to a bean
 * class in a programmatic type-safe way with help of the
 * {@link ConstraintMapping} API.
 *
 * @param <C> The type of a concrete sub type. Following to the
 * "self referencing generic type" pattern each sub type has to be
 * parametrized with itself.
 * @param <A> The constraint annotation type represented by a concrete sub type.
 *
 * @author Hardy Ferentschik
 * @author Gunnar Morling
 */
public abstract class ConstraintDef<C extends ConstraintDef<C, A>, A extends Annotation> extends AnnotationDef<C, A> {

	protected ConstraintDef(Class<A> constraintType) {
		super( constraintType );
	}

	protected ConstraintDef(ConstraintDef<?, A> original) {
		super( original );
	}

	@SuppressWarnings("unchecked")
	private C getThis() {
		return (C) this;
	}

	public C message(String message) {
		addParameter( ConstraintHelper.MESSAGE, message );
		return getThis();
	}

	public C groups(Class<?>... groups) {
		addParameter( ConstraintHelper.GROUPS, groups );
		return getThis();
	}

	@SuppressWarnings("unchecked")
	public C payload(Class<? extends Payload>... payload) {
		addParameter( ConstraintHelper.PAYLOAD, payload );
		return getThis();
	}
}
