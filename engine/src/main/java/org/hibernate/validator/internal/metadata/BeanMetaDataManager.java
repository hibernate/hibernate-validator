/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.internal.metadata;

import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;

/**
 * This manager is in charge of providing all constraint related meta data
 * required by the validation engine.
 *
 * @author Guillaume Smet
*/
public interface BeanMetaDataManager {

	<T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass);

	void clear();
}
