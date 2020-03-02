/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata.aggregated.rule;

import java.lang.invoke.MethodHandles;

import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A rule applying to the overriding methods of an inheritance hierarchy.
 *
 * @author Gunnar Morling
 */
public abstract class MethodConfigurationRule {

	protected static final Log LOG = LoggerFactory.make( MethodHandles.lookup() );

	/**
	 * Applies this rule. Invoked for each pair of methods collected by a given
	 * executable meta data {@link org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData.Builder}.
	 *
	 * @param method The first method to check.
	 * @param otherMethod The other method to check.
	 *
	 * @throws jakarta.validation.ConstraintDeclarationException If this rule is violated by the two given methods.
	 */
	public abstract void apply(ConstrainedExecutable method, ConstrainedExecutable otherMethod);

	/**
	 * Whether {@code otherClazz} is a strict subtype of {@code clazz} or not.
	 * @param clazz the super type to check against
	 * @param otherClazz the subtype to check
	 *
	 * @return {@code true} if {@code otherClazz} is a strict subtype of {@code clazz}, {@code false} otherwise
	 */
	protected boolean isStrictSubType(Class<?> clazz, Class<?> otherClazz) {
		return clazz.isAssignableFrom( otherClazz ) && !clazz.equals( otherClazz );
	}

	/**
	 * Whether {@code otherExecutable} is defined on a subtype of the declaring
	 * type of {@code executable} or not.
	 *
	 * @param executable the executable to check against
	 * @param otherExecutable the executable to check
	 *
	 * @return {@code true} if {@code otherExecutable} is defined on a subtype of the declaring type of
	 * {@code otherExecutable}, {@code false} otherwise
	 */
	protected boolean isDefinedOnSubType(ConstrainedExecutable executable, ConstrainedExecutable otherExecutable) {
		Class<?> clazz = executable.getCallable().getDeclaringClass();
		Class<?> otherClazz = otherExecutable.getCallable().getDeclaringClass();

		return isStrictSubType( clazz, otherClazz );
	}

	/**
	 * Whether {@code otherExecutable} is defined on a parallel of the declaring
	 * type of {@code executable} or not.
	 *
	 * @param executable the executable to check against
	 * @param otherExecutable the executable to check
	 *
	 * @return {@code true} if {@code otherExecutable} is defined on a parallel of the declaring type of
	 * {@code otherExecutable}, {@code false} otherwise
	 */
	protected boolean isDefinedOnParallelType(ConstrainedExecutable executable, ConstrainedExecutable otherExecutable) {
		Class<?> clazz = executable.getCallable().getDeclaringClass();
		Class<?> otherClazz = otherExecutable.getCallable().getDeclaringClass();

		return !( clazz.isAssignableFrom( otherClazz ) || otherClazz.isAssignableFrom( clazz ) );
	}
}
