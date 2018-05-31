/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.properties;

import java.lang.reflect.Type;

import org.hibernate.validator.internal.metadata.raw.ConstrainedElement.ConstrainedElementKind;
import org.hibernate.validator.internal.util.ExecutableHelper;
import org.hibernate.validator.internal.util.ExecutableParameterNameProvider;

/**
 * @author Marko Bekhta
 */
public interface Callable extends Constrainable {

	boolean hasReturnValue();

	boolean hasParameters();

	Class<?>[] getParameterTypes();

	Type[] getGenericParameterTypes();

	String getParameterName(ExecutableParameterNameProvider parameterNameProvider, int parameterIndex);

	boolean isPrivate();

	ConstrainedElementKind getConstrainedElementKind();

	String getSignature();

	Type getTypeOfParameter(int parameterIndex);

	boolean overrides(ExecutableHelper executableHelper, Callable superTypeMethod);

	boolean isResolvedToSameMethodInHierarchy(ExecutableHelper executableHelper, Class<?> mainSubType, Callable superTypeMethod);

}
