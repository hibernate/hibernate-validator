/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
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
