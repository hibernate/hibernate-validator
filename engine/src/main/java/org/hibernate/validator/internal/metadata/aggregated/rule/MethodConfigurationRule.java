/*
* JBoss, Home of Professional Open Source
* Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual contributors
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* http://www.apache.org/licenses/LICENSE-2.0
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.hibernate.validator.internal.metadata.aggregated.rule;

import org.hibernate.validator.internal.metadata.raw.ConstrainedExecutable;
import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;

/**
 * A rule applying to the overriding methods of an inheritance hierarchy.
 *
 * @author Gunnar Morling
 */
public abstract class MethodConfigurationRule {

	protected static final Log log = LoggerFactory.make();

	/**
	 * Applies this rule. Invoked for each pair of methods collected by a given
	 * executable meta data {@link org.hibernate.validator.internal.metadata.aggregated.ExecutableMetaData.Builder}.
	 *
	 * @param method The first method to check.
	 * @param otherMethod The other method to check.
	 *
	 * @throws javax.validation.ConstraintDeclarationException If this rule is violated by the two given methods.
	 */
	public abstract void apply(ConstrainedExecutable method, ConstrainedExecutable otherMethod);

	/**
	 * Whether {@code otherClazz} is a strict subtype of {@code clazz} or not.
	 */
	protected boolean isStrictSubType(Class<?> clazz, Class<?> otherClazz) {
		return clazz.isAssignableFrom( otherClazz ) && !clazz.equals( otherClazz );
	}

	/**
	 * Whether {@code otherExecutable} is defined on a subtype of the declaring
	 * type of {@code executable} or not.
	 */
	protected boolean isDefinedOnSubType(ConstrainedExecutable executable, ConstrainedExecutable otherExecutable) {
		Class<?> clazz = executable.getLocation().getDeclaringClass();
		Class<?> otherClazz = otherExecutable.getLocation().getDeclaringClass();

		return isStrictSubType( clazz, otherClazz );
	}

	/**
	 * Whether {@code otherExecutable} is defined on a parallel of the declaring
	 * type of {@code executable} or not.
	 */
	protected boolean isDefinedOnParallelType(ConstrainedExecutable executable, ConstrainedExecutable otherExecutable) {
		Class<?> clazz = executable.getLocation().getDeclaringClass();
		Class<?> otherClazz = otherExecutable.getLocation().getDeclaringClass();

		return !( clazz.isAssignableFrom( otherClazz ) || otherClazz.isAssignableFrom( clazz ) );
	}
}
