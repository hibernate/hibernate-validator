/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.properties;

import java.lang.reflect.Type;

import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * @author Marko Bekhta
 * @author Guillaume Smet
 */
public interface Callable extends Constrainable {

	boolean hasReturnValue();

	boolean hasParameters();

	int getParameterCount();

	Type getParameterGenericType(int index);

	Class<?>[] getParameterTypes();

	String getParameterName(ExecutableParameterNameProvider parameterNameProvider, int parameterIndex);

	boolean isPrivate();

	Signature getSignature();

	boolean overrides(ExecutableHelper executableHelper, Callable superTypeMethod);

	boolean isResolvedToSameMethodInHierarchy(ExecutableHelper executableHelper, Class<?> mainSubType, Callable superTypeMethod);

}
