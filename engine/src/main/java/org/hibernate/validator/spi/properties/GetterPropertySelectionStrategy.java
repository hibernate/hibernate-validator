/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.spi.properties;

import java.util.Set;

import org.hibernate.validator.Incubating;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;

/**
 * Used to define JavaBeans property detection algorithm. The default
 * implementation ({@link DefaultGetterPropertySelectionStrategy}) uses next definition
 * of JavaBeans property:
 * <p>
 * A JavaBeans method is considered to be a valid getter method (property), if
 * one of the next rules can be applied to it:
 * </p>
 * <ul>
 * <li>its name starts with "get" and it has a return type but no parameters</li>
 * <li>its name starts with "is", it has no parameters and is returning {@code boolean}</li>
 * <li>its name starts with "has", it has no parameters and is returning {@code boolean}.</li>
 * </ul>
 * <p>
 * The last rule is Hibernate Validator specific one and is not mandated by the JavaBeans spec.
 *
 * @author Marko Bekhta
 * @since 6.1.0
 */
@Incubating
public interface GetterPropertySelectionStrategy {

	/**
	 * Determines if a given {@link ConstrainableExecutable} is a valid JavaBeans getter method (property).
	 *
	 * @param executable the {@link ConstrainableExecutable} under test
	 *
	 * @return {@code true} if given executable is a valid JavaBeans property, {@code false} otherwise
	 */
	boolean isGetter(ConstrainableExecutable executable);

	/**
	 * Performs a transformation of JavaBeans method name to its corresponding property name.
	 * For example by removing the prefixes like {@code get}/{@code is} etc.
	 *
	 * @param method the {@link ConstrainableExecutable} which methods name should be transformed
	 *
	 * @return a property name of a given executable
	 */
	String getPropertyName(ConstrainableExecutable method);

	/**
	 * Gives a set of possible method names from based on a getter property name. Usually it means
	 * a property name prefixed with something like "get", "is", "has" etc.
	 *
	 * @param propertyName getter property name
	 *
	 * @return {@link Set} of possible method names
	 */
	Set<String> getGetterMethodNameCandidates(String propertyName);

}
