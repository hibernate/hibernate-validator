/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Red Hat Inc. and Hibernate Authors
 */
package org.hibernate.validator.internal.metadata;

import org.hibernate.validator.metadata.BeanMetaDataClassNormalizer;

/**
 * The default implementation of {@link BeanMetaDataClassNormalizer}.
 * <p>
 * Simply returns the provided class.
 *
 * @author Guillaume Smet
 */
public class DefaultBeanMetaDataClassNormalizer implements BeanMetaDataClassNormalizer {

	@Override
	public <T> Class<T> normalize(Class<T> beanClass) {
		return beanClass;
	}
}
