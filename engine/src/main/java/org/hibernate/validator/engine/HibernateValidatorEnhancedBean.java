/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.engine;

import org.hibernate.validator.Incubating;

/**
 * Hibernate Validator specific marker interface. Beans implementing this interface
 * would use corresponding {@link HibernateValidatorEnhancedBean#$$_hibernateValidator_getFieldValue(String)}
 * and {@link HibernateValidatorEnhancedBean#$$_hibernateValidator_getGetterValue(String)} methods to retrieve
 * bean property values instead of using reflection or any other means.
 * <p>
 * It is important to keep in mind that in case of explicit implementation of this interface
 * access to all possible constrained getters and fields should be provided, for a class implementing
 * the interface and all its super classes as well. Otherwise unexpected {@link IllegalArgumentException}
 * could be thrown by the Hibernate Validator engine.
 *
 * @author Marko Bekhta
 * @since 6.1
 */
@Incubating
public interface HibernateValidatorEnhancedBean {

	String GET_FIELD_VALUE_METHOD_NAME = "$$_hibernateValidator_getFieldValue";

	String GET_GETTER_VALUE_METHOD_NAME = "$$_hibernateValidator_getGetterValue";

	/**
	 * @param name the name of a field property of interest.
	 *
	 * @return the value of the field named {@code name} of the current bean.
	 *
	 * @throws IllegalArgumentException in case no field could be found for the given name.
	 */
	Object $$_hibernateValidator_getFieldValue(String name);

	/**
	 * @param name the name of a getter of interest.
	 *
	 * @return the value returned by the getter named {@code name} of the current bean.
	 *
	 * @throws IllegalArgumentException in case when no getter property could be found for the given name.
	 */
	Object $$_hibernateValidator_getGetterValue(String name);
}
