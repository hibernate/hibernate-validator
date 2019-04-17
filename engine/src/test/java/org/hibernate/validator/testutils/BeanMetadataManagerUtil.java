/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.testutils;

import org.hibernate.validator.internal.engine.constrainedtype.JavaBeanConstrainedType;
import org.hibernate.validator.internal.metadata.BeanMetaDataManager;
import org.hibernate.validator.internal.metadata.aggregated.BeanMetaData;

/**
 * A helper providing useful functions for simplifying metadata retrival in tests.
 *
 * @author Marko Bekhta
 */
public final class BeanMetadataManagerUtil {

	private BeanMetadataManagerUtil() {
	}

	/**
	 * @return a {@link BeanMetaData} for a given {@code clazz} created by {@code manager}.
	 */
	public static <T> BeanMetaData<T> getBeanMetadata(BeanMetaDataManager manager, Class<T> clazz) {
		return manager.getBeanMetaData( new JavaBeanConstrainedType<>( clazz ) );
	}
}
