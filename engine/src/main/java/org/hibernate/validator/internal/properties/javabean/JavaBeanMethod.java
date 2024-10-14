/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.properties.javabean;

import java.lang.reflect.Method;
import java.lang.reflect.TypeVariable;

import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;

/**
 * @author Guillaume Smet
 */
public class JavaBeanMethod extends JavaBeanExecutable<Method> {

	JavaBeanMethod(Method method) {
		super( method, method.getGenericReturnType() != void.class );
	}

	@Override
	public ConstrainedElementKind getConstrainedElementKind() {
		return ConstrainedElementKind.METHOD;
	}

	@Override
	public TypeVariable<?>[] getTypeParameters() {
		return executable.getReturnType().getTypeParameters();
	}
}
