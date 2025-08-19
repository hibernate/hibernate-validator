/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.engine.valuecontext;

import org.hibernate.validator.internal.engine.path.ModifiablePath;
import org.hibernate.validator.internal.metadata.facets.Validatable;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * @author Marko Bekhta
 */
public final class ExecutableValueContext<T, V> extends ValueContext<T, V> {

	ExecutableValueContext(ValueContext<?, ?> parentContext, ExecutableParameterNameProvider parameterNameProvider, T currentBean, Validatable validatable, ModifiablePath propertyPath) {
		super( parentContext, parameterNameProvider, currentBean, validatable, propertyPath );
	}

	@Override
	public boolean isBeanAlreadyValidated(Object value, Class<?> group) {
		// executables start with the root bean as a value of an object that that executable is "called" on,
		// but we haven't really validated that root, so we need to ignore it:
		return false;
	}

	@Override
	public void markCurrentGroupAsProcessed() {
		// do nothing
	}

	@Override
	protected boolean isProcessedForGroup(Class<?> group) {
		return false;
	}
}
