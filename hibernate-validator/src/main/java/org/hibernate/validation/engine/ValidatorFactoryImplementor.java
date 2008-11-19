package org.hibernate.validation.engine;

import javax.validation.ValidatorFactory;

/**
 * @author Emmanuel Bernard
 */
public interface ValidatorFactoryImplementor extends ValidatorFactory {
	/**
	 * Gives access to the required parsed meta data.
	 * This never returns an null object
	 */
	<T> MetaDataProviderImpl<T> getMetadataProvider(Class<T> clazz);
}
