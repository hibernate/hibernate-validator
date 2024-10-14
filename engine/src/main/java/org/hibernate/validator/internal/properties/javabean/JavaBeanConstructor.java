/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.reflect.Constructor;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;

/**
 * @author Guillaume Smet
 */
public class JavaBeanConstructor extends JavaBeanExecutable<Constructor<?>> {

	public JavaBeanConstructor(Constructor<?> executable) {
		super( executable, true );
	}

	@Override
	public String getName() {
		return getDeclaringClass().getSimpleName();
	}

	@Override
	public ConstrainedElementKind getConstrainedElementKind() {
		return ConstrainedElementKind.CONSTRUCTOR;
	}

	@Override
	public TypeVariable<?>[] getTypeParameters() {
		return executable.getDeclaringClass().getTypeParameters();
	}
}
