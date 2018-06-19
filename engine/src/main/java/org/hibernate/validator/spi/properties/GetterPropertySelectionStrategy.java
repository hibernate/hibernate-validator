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
 * Used to define the strategy to detect the getter of a bean. The default
 * implementation ({@link DefaultGetterPropertySelectionStrategy}) uses the JavaBeans
 * naming convention:
 * <p>
 * A JavaBean's method is considered to be a valid getter method (property), if
 * one of the next rules can be applied to it:
 * </p>
 * <ul>
 * <li>its name starts with "get" and it has a return type but no parameters</li>
 * <li>its name starts with "is", it has no parameters and is returning {@code boolean}</li>
 * <li>its name starts with "has", it has no parameters and is returning {@code boolean}.</li>
 * </ul>
 * <p>
 * The last rule is specific to Hibernate Validator and is not mandated by the JavaBeans specification.
 *
 * @author Marko Bekhta
 * @since 6.1.0
 */
@Incubating
public interface GetterPropertySelectionStrategy {

	/**
	 * Determines if a given {@link ConstrainableExecutable} is a getter method (property).
	 *
	 * @param executable a {@link ConstrainableExecutable}
	 *
	 * @return {@code true} if the given executable should be considered a getter, {@code false} otherwise
	 */
	boolean isGetter(ConstrainableExecutable executable);

	/**
	 * Performs a transformation of a getter method name to its corresponding property name.
	 * For example by removing the prefixes like {@code get}/{@code is} etc.
	 *
	 * @param executable a {@link ConstrainableExecutable}
	 *
	 * @return the property name corresponding to the given executable
	 *
	 * @throws IllegalArgumentException if a property name cannot be constructed
	 */
	String getPropertyName(ConstrainableExecutable executable);

	/**
	 * Gives a set of possible method names based on a property name. Usually, it means
	 * a property name prefixed with something like "get", "is", "has" etc.
	 *
	 * @param propertyName a property name
	 *
	 * @return the {@link Set} of possible getter names
	 */
	Set<String> getGetterMethodNameCandidates(String propertyName);

}
