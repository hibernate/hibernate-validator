/*
 * Hibernate Validator, declare and validate application constraints
 *
 * License: Apache License, Version 2.0
 * See the license.txt file in the root directory or <http://www.apache.org/licenses/LICENSE-2.0>.
 */
package org.hibernate.validator.metadata;

import org.hibernate.validator.Incubating;

/**
 * Define how the validated class is normalized before being used as the key to get the bean metadata.
 * <p>
 * In the case of the predefined scope validator factory, we have to register all the classes that will ever be
 * validated. To validate method calls, frameworks usually generate proxies to intercept the calls. Such proxies might
 * be hard to register in the predefined scope validator factory as they are generated code.
 * <p>
 * This contract allows to normalize the class before obtaining the metadata from the
 * {@code PredefinedScopeBeanMetaDataManager} so that we only have to register the original bean class and not the proxy
 * class.
 * <p>
 * Apart from avoiding the need to register the class, it also avoids generating unnecessary metadata for the proxy
 * classes.
 *
 * @author Guillaume Smet
 * @since 6.1
 */
@Incubating
public interface BeanMetaDataClassNormalizer {

	/**
	 * Normalizes the provided class as the key used to get the bean metadata from the
	 * {@code PredefinedScopeBeanMetaDataManager}.
	 *
	 * @param beanClass the original bean class
	 * @return the normalized class
	 */
	<T> Class<? super T> normalize(Class<T> beanClass);
}
