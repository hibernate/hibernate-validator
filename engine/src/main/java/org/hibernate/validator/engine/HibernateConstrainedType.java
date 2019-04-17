/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.engine;

import java.util.List;

/**
 * An interface that represents a type/class of a bean that will be validated.
 * Based on this type set of constraints will be determined, and applied to the
 * validated object.
 *
 * @author Marko Bekhta
 * @since 6.1
 */
public interface HibernateConstrainedType<T> {

	/**
	 * @return a class of an object that will be validated.
	 */
	Class<T> getActuallClass();

	List<HibernateConstrainedType<? super T>> getHierarchy();

	boolean isInterface();
}
