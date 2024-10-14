/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
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
